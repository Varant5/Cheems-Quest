package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.persistence.EncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class BossApiTest {

    private static final String ENRAGED = "Abominable Snow Doge is ENRAGED! The air turns to ice.";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SettableRandom rng;

    @Autowired
    private EncounterRepository encounters;

    @Autowired
    private TransactionTemplate transaction;

    private MockMvc mvc;

    private String id;

    @BeforeEach
    void climbToBonkPeak() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
        id = TestPlayers.createCharacter(mvc);
        TestPlayers.completeQuest(context, id, "LEG_DAY");
        TestPlayers.gainExperience(context, id, 500);
        for (String direction : new String[]{"NORTH", "WEST", "WEST"}) {
            move(direction).andExpect(status().isOk());
        }
        move("NORTH").andExpect(jsonPath("$.encounter.enemies[0].code").value("SNOW_DOGE"));
        rng.set(0.2);
    }

    private ResultActions move(String direction) throws Exception {
        return mvc.perform(post("/api/characters/{id}/move", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\": \"" + direction + "\"}"));
    }

    private ResultActions bonk() throws Exception {
        return mvc.perform(post("/api/characters/{id}/encounter/action", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"skill\": \"BONK\"}"));
    }

    private void setBossHp(int hp) {
        transaction.executeWithoutResult(status -> encounters
                .findByCharacterIdAndStatus(UUID.fromString(id), BattleStatus.PLAYER_TURN).orElseThrow()
                .updateEnemyHp(0, hp));
    }

    @Test
    void snowDogeFightsCalmlyAboveHalfHp() throws Exception {
        bonk().andExpect(jsonPath("$.log", not(hasItem(ENRAGED))))
                .andExpect(jsonPath("$.log", hasItem("Abominable Snow Doge uses Bonk!")));
    }

    @Test
    void snowDogeEnragesWhenPushedBelowHalfAndBreathesFrost() throws Exception {
        setBossHp(76);

        bonk().andExpect(jsonPath("$.log", hasItems(ENRAGED, "Abominable Snow Doge uses Frost Breath!")));

        bonk().andExpect(jsonPath("$.log", not(hasItem(ENRAGED))));
    }

    @Test
    void yetiHuntUnlocksAfterLegDay() throws Exception {
        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[4].code").value("YETI_HUNT"))
                .andExpect(jsonPath("$[4].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[4].target").value("Abominable Snow Doge"))
                .andExpect(jsonPath("$[4].rewardItem").value("Snow Crown"));
    }
}
