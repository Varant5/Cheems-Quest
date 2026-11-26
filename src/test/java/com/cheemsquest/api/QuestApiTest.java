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
class QuestApiTest {

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

    @Test
    void listsAvailableQuest() throws Exception {
        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("KAREN_PROBLEMS"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].objectiveType").value("DEFEAT_ENEMY"))
                .andExpect(jsonPath("$[0].target").value("Wild Karen Doge"))
                .andExpect(jsonPath("$[0].progress").value(0))
                .andExpect(jsonPath("$[0].requiredAmount").value(3))
                .andExpect(jsonPath("$[0].rewardExp").value(60))
                .andExpect(jsonPath("$[0].rewardItem").value("Stonks Helmet"));
    }

    @Test
    void listsVisitQuest() throws Exception {
        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[1].code").value("MYSTERY_SMELL"))
                .andExpect(jsonPath("$[1].objectiveType").value("VISIT_LOCATION"))
                .andExpect(jsonPath("$[1].target").value("Much Swamp"))
                .andExpect(jsonPath("$[1].requiredAmount").value(1))
                .andExpect(jsonPath("$[1].rewardItem").value("Cheemsburger"));
    }

    @Test
    void enteringTheTargetLocationCompletesVisitQuest() throws Exception {
        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "MYSTERY_SMELL"));

        move("NORTH").andExpect(jsonPath("$.log").isEmpty());
        move("WEST")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log[0]").value("Quest progress: Much Mystery Smell 1/1."))
                .andExpect(jsonPath("$.log[1]").value("QUEST COMPLETE! Much Mystery Smell. Much hero. Very reward."))
                .andExpect(jsonPath("$.log[2]").value("Cheems gains 25 EXP."))
                .andExpect(jsonPath("$.log[3]").value("Cheems receives Cheemsburger (COMMON)."));

        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[1].progress").value(1))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.experience").value(25));
    }

    @Test
    void visitingWithoutAcceptingDoesNothing() throws Exception {
        move("NORTH");
        move("WEST").andExpect(jsonPath("$.log").isEmpty());

        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[1].status").value("AVAILABLE"));
    }

    private ResultActions move(String direction) throws Exception {
        return mvc.perform(post("/api/characters/{id}/move", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\": \"" + direction + "\"}"));
    }

    @Test
    void acceptingMakesQuestActive() throws Exception {
        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "KAREN_PROBLEMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void cannotAcceptTwice() throws Exception {
        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "KAREN_PROBLEMS"));

        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "KAREN_PROBLEMS"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Quest Karen Problems was already accepted. Such eager."));
    }

    @Test
    void unknownQuestIs404() throws Exception {
        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "FIND_THE_CHEESEBURGER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void killsCountThroughRealFights() throws Exception {
        mvc.perform(post("/api/characters/{id}/quests/{code}/accept", id, "KAREN_PROBLEMS"));
        rng.set(0.5);
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "EAST"}
                """));
        for (String skill : new String[]{"BONK", "BONK", "BITE"}) {
            mvc.perform(post("/api/characters/{id}/encounter/action", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"skill\": \"" + skill + "\"}"));
        }

        mvc.perform(get("/api/characters/{id}/quests", id))
                .andExpect(jsonPath("$[0].progress").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
