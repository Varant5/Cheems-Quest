package com.cheemsquest.persistence;

import com.cheemsquest.engine.model.Stats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameCharacterTest {

    private final GameCharacter cheems = new GameCharacter(null, "Cheems", new Stats(65, 14, 3), null);

    @Test
    void experienceBelowThresholdKeepsLevel() {
        int levels = cheems.gainExperience(49);

        assertThat(levels).isZero();
        assertThat(cheems.getLevel()).isEqualTo(1);
        assertThat(cheems.getExperience()).isEqualTo(49);
        assertThat(cheems.experienceForNextLevel()).isEqualTo(50);
    }

    @Test
    void levelUpRaisesStatsAndRestoresHp() {
        cheems.updateHp(10);

        int levels = cheems.gainExperience(50);

        assertThat(levels).isEqualTo(1);
        assertThat(cheems.getLevel()).isEqualTo(2);
        assertThat(cheems.stats()).isEqualTo(new Stats(73, 16, 4));
        assertThat(cheems.getCurrentHp()).isEqualTo(73);
        assertThat(cheems.experienceForNextLevel()).isEqualTo(150);
    }

    @Test
    void bigRewardCanGrantSeveralLevels() {
        int levels = cheems.gainExperience(300);

        assertThat(levels).isEqualTo(3);
        assertThat(cheems.getLevel()).isEqualTo(4);
        assertThat(cheems.stats()).isEqualTo(new Stats(89, 20, 6));
    }
}
