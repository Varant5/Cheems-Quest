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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class ExitGateApiTest {

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

    @Test
    void towerIsClosedToLowLevelCheems() throws Exception {
        move("NORTH");

        move("NORTH")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Such gate. To enter Buff Doge Tower you need level 3."));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.location.code").value("WOW_FOREST"));
    }

    @Test
    void towerOpensAtLevelThree() throws Exception {
        TestPlayers.gainExperience(context, id, 150);
        move("NORTH");

        move("NORTH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.code").value("BUFF_DOGE_TOWER"));
    }

    @Test
    void mapShowsTheGateOnlyOnTheGuardedSide() throws Exception {
        mvc.perform(get("/api/world/map"))
                .andExpect(jsonPath("$.locations[0].code").value("BUFF_DOGE_TOWER"))
                .andExpect(jsonPath("$.locations[0].gates").isEmpty())
                .andExpect(jsonPath("$.locations[2].code").value("WOW_FOREST"))
                .andExpect(jsonPath("$.locations[2].gates.NORTH[0]").value("level 3"));
    }
}
