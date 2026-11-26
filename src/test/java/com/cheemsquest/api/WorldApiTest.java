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
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class WorldApiTest {

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
    void newCharacterKnowsOnlyHomeAndSeesItsNeighboursInTheFog() throws Exception {
        mvc.perform(get("/api/characters/{id}/map", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.width").value(2))
                .andExpect(jsonPath("$.height").value(2))
                .andExpect(jsonPath("$.locations[*].code", contains("WOW_FOREST", "CHEEMS_DOGHOUSE", "KARENS_BASEMENT")))
                .andExpect(jsonPath("$.locations[0].name").value("???"))
                .andExpect(jsonPath("$.locations[0].discovered").value(false))
                .andExpect(jsonPath("$.locations[0].exits").isEmpty())
                .andExpect(jsonPath("$.locations[1].name").value("Cheems' Doghouse"))
                .andExpect(jsonPath("$.locations[1].discovered").value(true))
                .andExpect(jsonPath("$.locations[1].x").value(0))
                .andExpect(jsonPath("$.locations[1].y").value(1))
                .andExpect(jsonPath("$.locations[1].safe").value(true))
                .andExpect(jsonPath("$.locations[1].exits.NORTH").value("WOW_FOREST"))
                .andExpect(jsonPath("$.locations[1].exits.EAST").value("KARENS_BASEMENT"))
                .andExpect(jsonPath("$.locations[2].x").value(1))
                .andExpect(jsonPath("$.locations[2].y").value(1));
    }

    @Test
    void walkingRevealsTheMapAndItsGates() throws Exception {
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "NORTH"}
                """)).andExpect(status().isOk());

        mvc.perform(get("/api/characters/{id}/map", id))
                .andExpect(jsonPath("$.width").value(3))
                .andExpect(jsonPath("$.height").value(3))
                .andExpect(jsonPath("$.locations[*].code", contains(
                        "BUFF_DOGE_TOWER", "MUCH_SWAMP", "WOW_FOREST", "CHEEMS_DOGHOUSE", "KARENS_BASEMENT")))
                .andExpect(jsonPath("$.locations[0].discovered").value(false))
                .andExpect(jsonPath("$.locations[0].gates").isEmpty())
                .andExpect(jsonPath("$.locations[2].name").value("Wow Forest"))
                .andExpect(jsonPath("$.locations[2].discovered").value(true))
                .andExpect(jsonPath("$.locations[2].gates.NORTH[0]").value("level 3"));
    }

    @Test
    void cannotPeekAtAnotherPlayersMap() throws Exception {
        TestPlayers.newPlayer(context).perform(get("/api/characters/{id}/map", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void mapRequiresLogin() throws Exception {
        TestPlayers.anonymous(context).perform(get("/api/characters/{id}/map", id))
                .andExpect(status().isUnauthorized());
    }
}
