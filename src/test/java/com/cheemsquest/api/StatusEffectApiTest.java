package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class StatusEffectApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SettableRandom rng;

    private MockMvc mvc;

    private String id;

    @BeforeEach
    void createCharacter() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
        id = TestPlayers.createCharacter(mvc);
    }

    private ResultActions move(String direction) throws Exception {
        return mvc.perform(post("/api/characters/{id}/move", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\": \"" + direction + "\"}"));
    }

    private ResultActions action(String body) throws Exception {
        return mvc.perform(post("/api/characters/{id}/encounter/action", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void soggyShibePoisonsCheemsAndThePoisonLastsBetweenRequests() throws Exception {
        move("NORTH");
        rng.set(0.2);
        move("WEST").andExpect(jsonPath("$.encounter.enemies[0].code").value("SOGGY_SHIBE"));

        action("{\"skill\": \"BONK\"}")
                .andExpect(jsonPath("$.log", hasItem("Cheems is poisoned for 3 turns! Much sick.")))
                .andExpect(jsonPath("$.encounter.playerStatuses[0].type").value("POISON"))
                .andExpect(jsonPath("$.encounter.playerStatuses[0].turns").value(3));
        mvc.perform(get("/api/characters/{id}/encounter", id))
                .andExpect(jsonPath("$.playerStatuses[0].type").value("POISON"));

        action("{\"skill\": \"BONK\"}")
                .andExpect(jsonPath("$.log[0]").value("Cheems takes 2 poison damage."));
    }

    @Test
    void stunnedCheemsDoesNotLoseTheItem() throws Exception {
        TestPlayers.completeQuest(context, id, "LEG_DAY");
        TestPlayers.giveItem(context, id, "CHEEMSBURGER");
        move("NORTH");
        move("WEST");
        rng.set(0.2);
        move("WEST").andExpect(jsonPath("$.encounter.enemies[0].code").value("MOUNTAIN_GOAT_DOGE"));

        action("{\"skill\": \"BONK\"}")
                .andExpect(jsonPath("$.log", hasItem("Cheems is stunned! Such dizzy.")))
                .andExpect(jsonPath("$.encounter.playerStatuses[0].type").value("STUN"));

        action("{\"item\": \"CHEEMSBURGER\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log[0]").value("Cheems is stunned and can't move."));
        mvc.perform(get("/api/characters/{id}/inventory", id))
                .andExpect(jsonPath("$.items[?(@.code == 'CHEEMSBURGER')].quantity", hasItem(1)));
    }
}
