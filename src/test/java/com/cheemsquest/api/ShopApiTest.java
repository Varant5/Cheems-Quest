package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
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

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class ShopApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SettableRandom rng;

    @Autowired
    private TransactionTemplate transaction;

    @Autowired
    private GameCharacterRepository characters;

    private MockMvc mvc;

    private String id;

    @BeforeEach
    void createCharacter() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
        id = TestPlayers.createCharacter(mvc);
    }

    private void update(Consumer<GameCharacter> change) {
        transaction.executeWithoutResult(status ->
                change.accept(characters.findById(UUID.fromString(id)).orElseThrow()));
    }

    private ResultActions buy(String itemCode, int quantity) throws Exception {
        return mvc.perform(post("/api/characters/{id}/shop/buy", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\": \"%s\", \"quantity\": %d}".formatted(itemCode, quantity)));
    }

    private ResultActions use(String itemCode) throws Exception {
        String body = mvc.perform(get("/api/characters/{id}/inventory", id)).andReturn().getResponse().getContentAsString();
        String itemId = JsonPath.<List<String>>read(body, "$.items[?(@.code == '%s')].id".formatted(itemCode)).getFirst();
        return mvc.perform(post("/api/characters/{id}/inventory/{item}/use", id, itemId));
    }

    private void moveEast() throws Exception {
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "EAST"}
                """)).andExpect(status().isOk());
    }

    @Test
    void doghouseHasAShopWithThreeOffersSortedByPrice() throws Exception {
        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.location.shop").value(true));

        mvc.perform(get("/api/characters/{id}/shop", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Cheems' Doghouse"))
                .andExpect(jsonPath("$.gold").value(0))
                .andExpect(jsonPath("$.offers[*].code").value(contains(
                        "CHEEMSBURGER", "LUCKY_DICE", "DOGE_COIN")))
                .andExpect(jsonPath("$.offers[0].price").value(15))
                .andExpect(jsonPath("$.offers[0].effect").value("HEAL"))
                .andExpect(jsonPath("$.offers[0].effectPower").value(30))
                .andExpect(jsonPath("$.offers[2].price").value(500));
    }

    @Test
    void otherLocationsHaveNoShop() throws Exception {
        moveEast();

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.location.shop").value(false));
        mvc.perform(get("/api/characters/{id}/shop", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No shop in Karen's Basement. Such empty."));
        buy("CHEEMSBURGER", 1).andExpect(status().isConflict());
    }

    @Test
    void buyingPaysGoldAndStacksItems() throws Exception {
        update(cheems -> cheems.addGold(100));

        buy("CHEEMSBURGER", 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gold").value(70))
                .andExpect(jsonPath("$.items[1].code").value("CHEEMSBURGER"))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.items[1].id").isNotEmpty())
                .andExpect(jsonPath("$.items[1].slot").doesNotExist())
                .andExpect(jsonPath("$.items[1].effect").value("HEAL"));
    }

    @Test
    void cannotBuyWithoutEnoughGold() throws Exception {
        update(cheems -> cheems.addGold(499));

        buy("DOGE_COIN", 1)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Need 500 gold, have 499. Much poor."));
    }

    @Test
    void cannotBuyWhatTheShopDoesNotSell() throws Exception {
        update(cheems -> cheems.addGold(1000));

        buy("LEGENDARY_BONE", 1).andExpect(status().isNotFound());
        buy("CHEEMSBURGER", 100).andExpect(status().isBadRequest());
    }

    @Test
    void cheemsburgerHealsAndIsConsumed() throws Exception {
        update(cheems -> {
            cheems.addGold(15);
            cheems.updateHp(20);
        });
        buy("CHEEMSBURGER", 1);

        use("CHEEMSBURGER")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hp").value(50))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void cheemsburgerIsSavedAtFullHp() throws Exception {
        update(cheems -> cheems.addGold(15));
        buy("CHEEMSBURGER", 1);

        use("CHEEMSBURGER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Already at full HP. Save the Cheemsburger for later."));
    }

    @Test
    void dogeCoinGivesPermanentAttack() throws Exception {
        update(cheems -> cheems.addGold(500));
        buy("DOGE_COIN", 1);

        use("DOGE_COIN")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attack").value(26))
                .andExpect(jsonPath("$.gold").value(0));

        mvc.perform(get("/api/characters/{id}", id))
                .andExpect(jsonPath("$.attack").value(26));
    }

    @Test
    void luckyDiceOnlyWorksInAFight() throws Exception {
        update(cheems -> cheems.addGold(40));
        buy("LUCKY_DICE", 1);

        use("LUCKY_DICE")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Lucky Dice only works in a fight. Save it for a bonk."));
    }

    @Test
    void consumablesCannotBeEquippedAndGearCannotBeUsed() throws Exception {
        update(cheems -> cheems.addGold(15));
        buy("CHEEMSBURGER", 1);
        String body = mvc.perform(get("/api/characters/{id}/inventory", id)).andReturn().getResponse().getContentAsString();
        String burger = JsonPath.read(body, "$.items[1].id");

        mvc.perform(post("/api/characters/{id}/inventory/{item}/equip", id, burger))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot wear Cheemsburger. Try using it instead."));
        use("VERY_STICK")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Very Stick is not something you can use. Try equipping it."));
    }

    @Test
    void cannotSnackDuringAFight() throws Exception {
        update(cheems -> {
            cheems.addGold(15);
            cheems.updateHp(20);
        });
        buy("CHEEMSBURGER", 1);
        rng.set(0.5);
        moveEast();

        use("CHEEMSBURGER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Mid-fight? Use it as your fight action instead."));
    }
}
