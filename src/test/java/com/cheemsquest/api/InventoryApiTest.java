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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class InventoryApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SettableRandom rng;

    private String id;

    @BeforeEach
    void createCharacter() throws Exception {
        rng.reset();
        String body = mvc.perform(post("/api/characters").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "inventory_tester", "name": "Cheems"}
                        """))
                .andReturn().getResponse().getContentAsString();
        id = JsonPath.read(body, "$.id");
    }

    private String stickId() throws Exception {
        String body = mvc.perform(get("/api/characters/{id}/inventory", id)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.items[0].id");
    }

    @Test
    void newCharacterStartsWithVeryStickEquipped() throws Exception {
        mvc.perform(get("/api/characters/{id}/inventory", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].code").value("VERY_STICK"))
                .andExpect(jsonPath("$.items[0].slot").value("WEAPON"))
                .andExpect(jsonPath("$.items[0].rarity").value("COMMON"))
                .andExpect(jsonPath("$.items[0].bonus.attack").value(2))
                .andExpect(jsonPath("$.items[0].equipped").value(true))
                .andExpect(jsonPath("$.attack").value(16));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.attack").value(16));
    }

    @Test
    void unequipAndEquipChangeStats() throws Exception {
        String stick = stickId();

        mvc.perform(post("/api/characters/{id}/inventory/{item}/unequip", id, stick))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].equipped").value(false))
                .andExpect(jsonPath("$.attack").value(14));

        mvc.perform(post("/api/characters/{id}/inventory/{item}/equip", id, stick))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attack").value(16));
    }

    @Test
    void cannotChangeEquipmentDuringAFight() throws Exception {
        rng.set(0.5);
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "EAST"}
                """));

        mvc.perform(post("/api/characters/{id}/inventory/{item}/unequip", id, stickId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No time to change outfits mid-fight. Such danger!"));
    }

    @Test
    void unknownItemIs404() throws Exception {
        mvc.perform(post("/api/characters/{id}/inventory/{item}/equip", id, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
