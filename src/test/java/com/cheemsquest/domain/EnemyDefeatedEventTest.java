package com.cheemsquest.domain;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestRandomConfiguration.SettableRandom;
import com.cheemsquest.TestcontainersConfiguration;
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

    private UUID characterId;

    @BeforeEach
    void createCharacter() {
        rng.reset();
        characterId = characterService.create("event_tester", "Cheems").id();
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
        assertThat(log.lines()).containsExactly(
                "Cheems gains 160 EXP.",
                "LEVEL UP! Cheems is now level 3. Much stronger!",
                "Loot! Buff Doge drops Legendary Bone (LEGENDARY).");
    }

    @Test
    void karenDropsHerWigWhenLuckIsGood() {
        rng.set(0.2);

        GameLog log = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(log.lines()).containsExactly(
                "Cheems gains 20 EXP.",
                "Loot! Wild Karen Doge drops Karen's Wig (RARE).");
        InventorySlot wig = cheems().getInventory().getLast();
        assertThat(wig.getItem().getCode()).isEqualTo("KARENS_WIG");
        assertThat(wig.isEquipped()).isFalse();
    }

    @Test
    void karenDropsNothingWhenLuckIsBad() {
        GameLog log = defeat("KAREN_DOGE", "Wild Karen Doge", 20);

        assertThat(log.lines()).containsExactly("Cheems gains 20 EXP.");
        assertThat(cheems().getInventory()).hasSize(1);
    }
}
