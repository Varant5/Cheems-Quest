package com.cheemsquest.api;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.ItemRepository;
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
import java.util.function.Consumer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Roll 0.5: Wild Karen Doge (35 HP, 9 ATK, 2 DEF) always Bonks Cheems (3 DEF) for 6.
@SpringBootTest
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class CombatItemApiTest {

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
    void startFightWithKaren() throws Exception {
        rng.reset();
        mvc = TestPlayers.newPlayer(context);
        id = TestPlayers.createCharacter(mvc);
        rng.set(0.5);
        mvc.perform(post("/api/characters/{id}/move", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"direction": "EAST"}
                """)).andExpect(jsonPath("$.encounter.status").value("PLAYER_TURN"));
    }

    private void update(Consumer<GameCharacter> change) {
        transaction.executeWithoutResult(status ->
                change.accept(characters.findById(UUID.fromString(id)).orElseThrow()));
    }

    private void give(String itemCode) {
        update(cheems -> cheems.addItem(items.findById(itemCode).orElseThrow()));
    }

    private ResultActions action(String body) throws Exception {
        return mvc.perform(post("/api/characters/{id}/encounter/action", id)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions useItem(String itemCode) throws Exception {
        return action("{\"item\": \"" + itemCode + "\"}");
    }

    @Test
    void cheemsburgerHealsAndTakesTheTurn() throws Exception {
        give("CHEEMSBURGER");
        update(cheems -> cheems.updateHp(20));

        useItem("CHEEMSBURGER")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PLAYER_TURN"))
                .andExpect(jsonPath("$.log", contains(
                        "Cheems uses Cheemsburger!",
                        "Cheems heals 30 HP. Much yum.",
                        "Wild Karen Doge uses Bonk!",
                        "Cheems loses 6 HP.")))
                .andExpect(jsonPath("$.hp").value(44))
                .andExpect(jsonPath("$.encounter.enemies[0].hp").value(35));

        mvc.perform(get("/api/characters/{id}/inventory", id))
                .andExpect(jsonPath("$.items[*].code", not(hasItem("CHEEMSBURGER"))));
    }

    @Test
    void luckyDiceMakesTheNextAttackCritAcrossRequests() throws Exception {
        give("LUCKY_DICE");

        useItem("LUCKY_DICE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log[1]").value("Cheems feels lucky. The next attack will crit!"))
                .andExpect(jsonPath("$.encounter.lucky").value(true));

        mvc.perform(get("/api/characters/{id}/encounter", id))
                .andExpect(jsonPath("$.lucky").value(true));

        // Bonk: 16 * 1.5 - 2 = 22.
        action("{\"skill\": \"BONK\"}")
                .andExpect(jsonPath("$.log[1]").value("CRITICAL! Wild Karen Doge loses 22 HP."))
                .andExpect(jsonPath("$.encounter.lucky").value(false))
                .andExpect(jsonPath("$.encounter.enemies[0].hp").value(13));
    }

    @Test
    void luckDoesNotStack() throws Exception {
        give("LUCKY_DICE");
        give("LUCKY_DICE");
        useItem("LUCKY_DICE");

        useItem("LUCKY_DICE")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Already feeling lucky. Save the Lucky Dice for later."));
    }

    @Test
    void cheemsburgerIsSavedAtFullHp() throws Exception {
        give("CHEEMSBURGER");

        useItem("CHEEMSBURGER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Already at full HP. Save the Cheemsburger for later."));
    }

    @Test
    void dogeCoinNeedsACalmMoment() throws Exception {
        give("DOGE_COIN");

        useItem("DOGE_COIN")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Doge Coin needs a calm moment. Use it outside of a fight."));
    }

    @Test
    void gearIsNotAFightAction() throws Exception {
        give("SOGGY_RAINCOAT");

        useItem("SOGGY_RAINCOAT")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Soggy Raincoat is not something you can use. Try a skill instead."));
    }

    @Test
    void missingItemIs404() throws Exception {
        useItem("CHEEMSBURGER").andExpect(status().isNotFound());
    }

    @Test
    void actionNeedsExactlyOneOfSkillAndItem() throws Exception {
        give("CHEEMSBURGER");

        action("{\"skill\": \"BONK\", \"item\": \"CHEEMSBURGER\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.singleAction").value("send either a skill or an item"));
        action("{}").andExpect(status().isBadRequest());
    }
}
