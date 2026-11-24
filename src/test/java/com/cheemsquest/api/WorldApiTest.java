package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class WorldApiTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void mapPlacesTheSeededWorldOnAGrid() throws Exception {
        TestPlayers.newPlayer(context).perform(get("/api/world/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.width").value(3))
                .andExpect(jsonPath("$.height").value(3))
                .andExpect(jsonPath("$.locations[*].code", contains(
                        "BUFF_DOGE_TOWER", "MUCH_SWAMP", "WOW_FOREST", "CHEEMS_DOGHOUSE", "KARENS_BASEMENT")))
                .andExpect(jsonPath("$.locations[3].name").value("Cheems' Doghouse"))
                .andExpect(jsonPath("$.locations[3].x").value(1))
                .andExpect(jsonPath("$.locations[3].y").value(2))
                .andExpect(jsonPath("$.locations[3].safe").value(true))
                .andExpect(jsonPath("$.locations[3].exits.NORTH").value("WOW_FOREST"))
                .andExpect(jsonPath("$.locations[3].exits.EAST").value("KARENS_BASEMENT"))
                .andExpect(jsonPath("$.locations[0].safe").value(false));
    }

    @Test
    void mapRequiresLogin() throws Exception {
        TestPlayers.anonymous(context).perform(get("/api/world/map"))
                .andExpect(status().isUnauthorized());
    }
}
