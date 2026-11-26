package com.cheemsquest.domain;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.engine.quest.QuestStatus;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.InventorySlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
class EnemyDefeatedEventTest {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private GameCharacterRepository characters;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private SettableRandom rng;

    @Autowired
    private QuestService questService;

    @Autowired
    private AuthService authService;

    private UUID characterId;

    @BeforeEach
    void createCharacter() {
        rng.reset();
        PlayerView player = authService.register("event_tester", "much-secret-very-safe");
        characterId = characterService.create(player.id(), "Cheems").id();
    }

    private GameLog defeat(String enemyCode, String enemyName, int exp) {
        GameLog log = new GameLog();
        events.publishEvent(new EnemyDefeatedEvent(characterId, enemyCode, enemyName, exp, log));
        return log;
    }

    private GameCharacter cheems() {
        return characters.findById(characterId).orElseThrow();
    }

    @Test
    void defeatingBuffDogeGrantsExperienceLevelsAndGuaranteedLoot() {
        GameLog log = defeat("BUFF_DOGE", "Buff Doge", 160);

        GameCharacter cheems = cheems();
        assertThat(cheems.getLevel()).isEqualTo(3);
        assertThat(cheems.getExperience()).isEqualTo(160);
        assertThat(cheems.stats().maxHp()).isEqualTo(81);
        assertThat(cheems.getInventory()).extracting(slot -> slot.getItem().getCode())
                .containsExactly("VERY_STICK", "LEGENDARY_BONE");
        assertThat(cheems.getGold()).isEqualTo(58);
        assertThat(log.lines()).containsExactly(
                "Cheems gains 160 EXP.",
                "LEVEL UP! Cheems is now level 3. Much stronger!",
                "Cheems finds 58 gold.",
                "Loot! Buff Doge drops Legendary Bone (LEGENDARY).");
    }

    @Test
    void karenDropsHerWigWhenLuckIsGood() {
        rng.set(0.2);

        GameLog log = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(log.lines()).containsExactly(
                "Cheems gains 20 EXP.",
                "Cheems finds 6 gold.",
                "Loot! Wild Karen Doge drops Karen's Wig (RARE).");
        InventorySlot wig = cheems().getInventory().getLast();
        assertThat(wig.getItem().getCode()).isEqualTo("KARENS_WIG");
        assertThat(wig.isEquipped()).isFalse();
    }

    @Test
    void bonkingThreeKarensCompletesTheQuestAndGrantsRewards() {
        questService.accept(characterId, "KAREN_PROBLEMS");
        defeat("KAREN_DOGE", "Wild Karen Doge", 20);
        GameLog second = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        GameLog third = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(second.lines()).contains("Quest progress: Karen Problems 2/3.");
        assertThat(third.lines()).containsExactly(
                "Cheems gains 20 EXP.",
                "LEVEL UP! Cheems is now level 2. Much stronger!",
                "Cheems finds 10 gold.",
                "Quest progress: Karen Problems 3/3.",
                "QUEST COMPLETE! Karen Problems. Much hero. Very reward.",
                "Cheems gains 60 EXP.",
                "Cheems receives Stonks Helmet (EPIC).");
        assertThat(cheems().getExperience()).isEqualTo(120);
        assertThat(cheems().getInventory()).extracting(slot -> slot.getItem().getCode()).contains("STONKS_HELMET");
        assertThat(questService.list(characterId)).filteredOn(quest -> quest.code().equals("KAREN_PROBLEMS"))
                .singleElement().satisfies(quest -> assertThat(quest.status()).isEqualTo(QuestStatus.COMPLETED));

        GameLog fourth = defeat("KAREN_DOGE", "Wild Karen Doge", 20);
        assertThat(fourth.lines()).containsExactly("Cheems gains 20 EXP.", "Cheems finds 10 gold.");
    }

    @Test
    void otherEnemiesDoNotCountTowardsTheQuest() {
        questService.accept(characterId, "KAREN_PROBLEMS");

        GameLog log = defeat("LIL_DOGE", "Neighbor's Lil Doge", 8);

        assertThat(log.lines()).containsExactly("Cheems gains 8 EXP.", "Cheems finds 5 gold.");
        assertThat(questService.list(characterId).getFirst().progress()).isZero();
    }

    @Test
    void questsDoNotProgressBeforeBeingAccepted() {
        GameLog log = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(log.lines()).containsExactly("Cheems gains 20 EXP.", "Cheems finds 10 gold.");
        assertThat(questService.list(characterId).getFirst().status()).isEqualTo(QuestStatus.AVAILABLE);
    }

    @Test
    void lootedWigsCountTowardsTheCollectQuest() {
        questService.accept(characterId, "WIG_HUNT");
        rng.set(0.2);

        GameLog first = defeat("KAREN_DOGE", "Wild Karen Doge", 20);
        GameLog second = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(first.lines()).endsWith(
                "Loot! Wild Karen Doge drops Karen's Wig (RARE).",
                "Quest progress: Manager Material 1/2.");
        assertThat(second.lines()).containsExactly(
                "Cheems gains 20 EXP.",
                "Cheems finds 6 gold.",
                "Loot! Wild Karen Doge drops Karen's Wig (RARE).",
                "Quest progress: Manager Material 2/2.",
                "QUEST COMPLETE! Manager Material. Much hero. Very reward.",
                "Cheems gains 80 EXP.",
                "LEVEL UP! Cheems is now level 2. Much stronger!",
                "Cheems receives Spiky Collar (RARE).");
        assertThat(cheems().getInventory()).extracting(slot -> slot.getItem().getCode())
                .containsExactly("VERY_STICK", "KARENS_WIG", "KARENS_WIG", "SPIKY_COLLAR");
    }

    @Test
    void collectQuestIgnoresOtherItems() {
        questService.accept(characterId, "WIG_HUNT");
        questService.accept(characterId, "KAREN_PROBLEMS");

        defeat("KAREN_DOGE", "Wild Karen Doge", 20);
        defeat("KAREN_DOGE", "Wild Karen Doge", 20);
        GameLog third = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(third.lines()).endsWith("Cheems receives Stonks Helmet (EPIC).");
        assertThat(questService.list(characterId)).filteredOn(quest -> quest.code().equals("WIG_HUNT"))
                .singleElement().satisfies(quest -> assertThat(quest.progress()).isZero());
    }

    @Test
    void karenDropsOnlyGoldWhenLuckIsBad() {
        GameLog log = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(log.lines()).containsExactly("Cheems gains 20 EXP.", "Cheems finds 10 gold.");
        assertThat(cheems().getInventory()).hasSize(1);
        assertThat(cheems().getGold()).isEqualTo(10);
    }
}
