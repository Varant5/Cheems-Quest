package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Damage numbers assume a constant roll of 0.5 (neutral damage) unless stated otherwise.
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class EncounterApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SettableRandom rng;

    private String id;

    @BeforeEach
    void createCharacter() throws Exception {
        rng.reset();
        String body = postJson("/api/characters", """
                {"username": "encounter_tester", "name": "Cheems"}
                """)
                .andReturn().getResponse().getContentAsString();
        id = JsonPath.read(body, "$.id");
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions move(String direction) throws Exception {
        return postJson("/api/characters/" + id + "/move", "{\"direction\": \"" + direction + "\"}");
    }

    private ResultActions act(String skill) throws Exception {
        return postJson("/api/characters/" + id + "/encounter/action", "{\"skill\": \"" + skill + "\"}");
    }

    @Test
    void enteringDangerousLocationCanStartAFight() throws Exception {
        rng.set(0.5);

        move("EAST")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You enter Karen's Basement. Wild Karen Doge blocks the path!"))
                .andExpect(jsonPath("$.encounter.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.encounter.enemies[0].code").value("KAREN_DOGE"))
                .andExpect(jsonPath("$.encounter.enemies[0].hp").value(35));

        mvc.perform(get("/api/characters/{id}/encounter", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enemies[0].name").value("Wild Karen Doge"));
    }

    @Test
    void peacefulWalkHasNoEncounter() throws Exception {
        move("EAST")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You enter Karen's Basement."))
                .andExpect(jsonPath("$.encounter").doesNotExist());

        mvc.perform(get("/api/characters/{id}/encounter", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void cannotRunFromAFight() throws Exception {
        rng.set(0.5);
        move("EAST");

        move("WEST")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("You can't run from Wild Karen Doge. Fight!"));
    }

    @Test
    void fightAgainstKarenEndsInVictoryAndStateIsPersistedBetweenRequests() throws Exception {
        rng.set(0.5);
        move("EAST");

        // Bonk: 14 - 2 = 12. Karen's Bonk: 9 - 3 = 6.
        act("BONK")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.log", contains(
                        "Cheems uses Bonk!",
                        "Wild Karen Doge loses 12 HP.",
                        "Wild Karen Doge uses Bonk!",
                        "Cheems loses 6 HP.")))
                .andExpect(jsonPath("$.hp").value(59))
                .andExpect(jsonPath("$.encounter.enemies[0].hp").value(23));

        // Bite: round(14 * 1.6) - 2 = 20.
        act("BITE")
                .andExpect(jsonPath("$.hp").value(53))
                .andExpect(jsonPath("$.encounter.enemies[0].hp").value(3));

        act("BONK")
                .andExpect(jsonPath("$.status").value("VICTORY"))
                .andExpect(jsonPath("$.log", hasItem("Wild Karen Doge is down!")))
                .andExpect(jsonPath("$.log", hasItem(containsString("VICTORY!"))))
                .andExpect(jsonPath("$.log[-1]").value("Cheems gains 20 EXP."))
                .andExpect(jsonPath("$.hp").value(53));

        mvc.perform(get("/api/characters/{id}/encounter", id))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.experience").value(20))
                .andExpect(jsonPath("$.experienceForNextLevel").value(50));
    }

    @Test
    void napInTheDoghouseRestoresHp() throws Exception {
        rng.set(0.5);
        move("EAST");
        act("BONK");
        act("BITE");
        act("BONK");

        move("WEST")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You enter Cheems' Doghouse. Much nap. HP fully restored."))
                .andExpect(jsonPath("$.hp").value(65));
    }

    @Test
    void defeatRespawnsCharacterAtHomeWithFullHp() throws Exception {
        move("NORTH");
        // 0.2: always an encounter, no crits, 94% damage. Buff Doge hits for 12, Cheems has 65 HP.
        rng.set(0.2);
        move("NORTH")
                .andExpect(jsonPath("$.encounter.enemies[0].code").value("BUFF_DOGE"));

        String status = "PLAYER_TURN";
        String lastBody = "";
        for (int round = 0; round < 10 && status.equals("PLAYER_TURN"); round++) {
            lastBody = act("BONK").andReturn().getResponse().getContentAsString();
            status = JsonPath.read(lastBody, "$.status");
        }

        assertThat(status).isEqualTo("DEFEAT");
        assertThat(JsonPath.<String>read(lastBody, "$.log[-1]")).contains("faints and wakes up in Cheems' Doghouse");
        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.hp").value(65))
                .andExpect(jsonPath("$.location.code").value("CHEEMS_DOGHOUSE"));
    }

    @Test
    void actingOutsideOfAFightIsNotAllowed() throws Exception {
        act("BONK")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Nothing to bonk here. Such peace."));
    }

    @Test
    void unknownSkillIs400() throws Exception {
        act("MUCH_LASER")
                .andExpect(status().isBadRequest());
    }

    @Test
    void targetOutOfRangeIsRejected() throws Exception {
        rng.set(0.5);
        move("EAST");

        postJson("/api/characters/" + id + "/encounter/action", """
                {"skill": "BONK", "target": 3}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("There is no enemy at position 3."));
    }
}
