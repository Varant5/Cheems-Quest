package com.cheemsquest.domain;

import com.cheemsquest.TestRandomConfiguration;
import com.cheemsquest.TestcontainersConfiguration;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    void defeatingBuffDogeGrantsExperienceAndLevels() {
        CharacterView created = characterService.create("event_tester", "Cheems");
        GameLog log = new GameLog();

        events.publishEvent(new EnemyDefeatedEvent(created.id(), "BUFF_DOGE", "Buff Doge", 160, log));

        GameCharacter cheems = characters.findById(created.id()).orElseThrow();
        assertThat(cheems.getLevel()).isEqualTo(3);
        assertThat(cheems.getExperience()).isEqualTo(160);
        assertThat(cheems.stats().maxHp()).isEqualTo(81);
        assertThat(log.lines()).containsExactly(
                "Cheems gains 160 EXP.",
                "LEVEL UP! Cheems is now level 3. Much stronger!");
    }
}
