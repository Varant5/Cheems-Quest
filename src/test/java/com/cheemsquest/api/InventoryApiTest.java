package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Item;
import com.cheemsquest.persistence.ItemRepository;
import com.jayway.jsonpath.JsonPath;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class InventoryApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SettableRandom rng;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private GameCharacterRepository characters;

    @Autowired
    private ItemRepository items;

    private MockMvc mvc;

    private String id;

    @BeforeEach
    void createCharacter() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
        id = TestPlayers.createCharacter(mvc);
    }

    private String stickId() throws Exception {
        String body = mvc.perform(get("/api/characters/{id}/inventory", id)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.items[0].id");
    }

    private void give(String itemCode, int copies) {
        transaction.executeWithoutResult(status -> {
            GameCharacter character = characters.findById(UUID.fromString(id)).orElseThrow();
            Item item = items.findById(itemCode).orElseThrow();
            for (int i = 0; i < copies; i++) {
                character.addItem(item);
            }
        });
    }

    private ResultActions sell(String itemCode, int quantity) throws Exception {
        return mvc.perform(post("/api/characters/{id}/inventory/sell", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\": \"%s\", \"quantity\": %d}".formatted(itemCode, quantity)));
    }

    private void startFight() throws Exception {
        rng.set(0.5);
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "EAST"}
                """));
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
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[0].sellValue").value(2))
                .andExpect(jsonPath("$.gold").value(0))
                .andExpect(jsonPath("$.attack").value(16));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.attack").value(16))
                .andExpect(jsonPath("$.gold").value(0));
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
        startFight();

        mvc.perform(post("/api/characters/{id}/inventory/{item}/unequip", id, stickId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No time to change outfits mid-fight. Such danger!"));
    }

    @Test
    void unknownItemIs404() throws Exception {
        mvc.perform(post("/api/characters/{id}/inventory/{item}/equip", id, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicatesAreGroupedSeparatelyFromEquippedCopy() throws Exception {
        give("VERY_STICK", 2);
        give("SOGGY_RAINCOAT", 3);

        mvc.perform(get("/api/characters/{id}/inventory", id))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].code").value("VERY_STICK"))
                .andExpect(jsonPath("$.items[0].equipped").value(true))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[1].code").value("VERY_STICK"))
                .andExpect(jsonPath("$.items[1].equipped").value(false))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.items[2].code").value("SOGGY_RAINCOAT"))
                .andExpect(jsonPath("$.items[2].quantity").value(3));
    }

    @Test
    void sellingPaysGoldAndShrinksTheStack() throws Exception {
        give("SOGGY_RAINCOAT", 3);

        sell("SOGGY_RAINCOAT", 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gold").value(10))
                .andExpect(jsonPath("$.items[1].quantity").value(1));

        sell("SOGGY_RAINCOAT", 1)
                .andExpect(jsonPath("$.gold").value(15))
                .andExpect(jsonPath("$.items.length()").value(1));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.gold").value(15));
    }

    @Test
    void cannotSellEquippedItem() throws Exception {
        sell("VERY_STICK", 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot sell what you are wearing. Unequip it first."));
    }

    @Test
    void cannotSellMoreThanOwned() throws Exception {
        give("SOGGY_RAINCOAT", 2);

        sell("SOGGY_RAINCOAT", 3)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Only 2 to sell, not 3. Much math."));
    }

    @Test
    void sellingUnknownItemIs404() throws Exception {
        sell("LEGENDARY_BONE", 1).andExpect(status().isNotFound());
    }

    @Test
    void sellingRequiresPositiveQuantity() throws Exception {
        give("SOGGY_RAINCOAT", 1);

        sell("SOGGY_RAINCOAT", 0).andExpect(status().isBadRequest());
    }

    @Test
    void cannotSellDuringAFight() throws Exception {
        give("SOGGY_RAINCOAT", 1);
        startFight();

        sell("SOGGY_RAINCOAT", 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No time for selling stuff mid-fight. Such danger!"));
    }
}
