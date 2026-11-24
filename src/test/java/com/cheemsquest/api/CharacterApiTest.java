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

import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class CharacterApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SettableRandom rng;

    private MockMvc mvc;

    @BeforeEach
    void logIn() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void createsCharacterInTheDoghouse() throws Exception {
        postJson("/api/characters", """
                {"name": "Cheems"}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/characters/")))
                .andExpect(jsonPath("$.name").value("Cheems"))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.hp").value(65))
                .andExpect(jsonPath("$.maxHp").value(65))
                .andExpect(jsonPath("$.location.code").value("CHEEMS_DOGHOUSE"))
                .andExpect(jsonPath("$.location.exits", contains("NORTH", "EAST")));
    }

    @Test
    void returnsCreatedCharacter() throws Exception {
        String id = TestPlayers.createCharacter(mvc);

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void listsOnlyMyCharacters() throws Exception {
        String first = TestPlayers.createCharacter(mvc);
        String second = TestPlayers.createCharacter(mvc);
        TestPlayers.createCharacter(TestPlayers.newPlayer(context));

        mvc.perform(get("/api/characters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", containsInAnyOrder(first, second)));
    }

    @Test
    void movingChangesLocation() throws Exception {
        String id = TestPlayers.createCharacter(mvc);

        postJson("/api/characters/" + id + "/move", """
                {"direction": "NORTH"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You enter Wow Forest."))
                .andExpect(jsonPath("$.location.code").value("WOW_FOREST"))
                .andExpect(jsonPath("$.location.exits", contains("NORTH", "SOUTH", "WEST")));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.location.code").value("WOW_FOREST"));
    }

    @Test
    void cannotWalkIntoAWall() throws Exception {
        String id = TestPlayers.createCharacter(mvc);

        postJson("/api/characters/" + id + "/move", """
                {"direction": "WEST"}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Not allowed"))
                .andExpect(jsonPath("$.detail", containsString("Much wall")));
    }

    @Test
    void unknownCharacterIs404() throws Exception {
        mvc.perform(get("/api/characters/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", endsWith("not found")));
    }

    @Test
    void invalidCreateRequestListsFieldErrors() throws Exception {
        postJson("/api/characters", """
                {"name": ""}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void unknownDirectionIs400() throws Exception {
        String id = TestPlayers.createCharacter(mvc);

        postJson("/api/characters/" + id + "/move", """
                {"direction": "UP"}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedIdIs400() throws Exception {
        mvc.perform(get("/api/characters/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestsWithoutTokenAre401() throws Exception {
        String id = TestPlayers.createCharacter(mvc);
        MockMvc anonymous = TestPlayers.anonymous(context);

        anonymous.perform(get("/api/characters/{id}", id))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Bearer")));
        anonymous.perform(post("/api/characters").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Cheems"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedTokenIs401() throws Exception {
        TestPlayers.as(context, "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2dlIn0.c3VjaC1mYWtl")
                .perform(get("/api/characters"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannotTouchAnotherPlayersCharacter() throws Exception {
        String mine = TestPlayers.createCharacter(mvc);
        MockMvc intruder = TestPlayers.newPlayer(context);

        intruder.perform(get("/api/characters/{id}", mine))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Paws off! This Cheems belongs to another player."));
        intruder.perform(post("/api/characters/{id}/move", mine).contentType(MediaType.APPLICATION_JSON).content("""
                        {"direction": "NORTH"}
                        """))
                .andExpect(status().isForbidden());
        intruder.perform(get("/api/characters/{id}/inventory", mine))
                .andExpect(status().isForbidden());
        intruder.perform(post("/api/characters/{id}/encounter/action", mine).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\": \"BONK\"}"))
                .andExpect(status().isForbidden());
        intruder.perform(post("/api/characters/{id}/quests/{code}/accept", mine, "KAREN_PROBLEMS"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/characters/{id}", mine))
                .andExpect(jsonPath("$.location.code").value("CHEEMS_DOGHOUSE"));
    }
}
