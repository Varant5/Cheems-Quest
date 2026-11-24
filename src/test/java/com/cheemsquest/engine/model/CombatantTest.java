package com.cheemsquest.engine.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombatantTest {

    private static final Stats STATS = new Stats(30, 10, 2);

    @ParameterizedTest
    @CsvSource({"0, 5, 1", "-5, 5, 1", "30, -1, 0", "30, 5, -1"})
    void statsRejectInvalidValues(int maxHp, int attack, int defense) {
        assertThatThrownBy(() -> new Stats(maxHp, attack, defense))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 31})
    void hpMustFitBetweenZeroAndMax(int hp) {
        assertThatThrownBy(() -> new Combatant("Karen Doge", STATS, hp))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hp must be in [0, 30]");
    }

    @Test
    void startsWithFullHp() {
        assertThat(new Combatant("Karen Doge", STATS).getHp()).isEqualTo(30);
    }

    @Test
    void damageIsCappedAtRemainingHp() {
        Combatant karen = new Combatant("Karen Doge", STATS, 5);

        int dealt = karen.takeDamage(12);

        assertThat(dealt).isEqualTo(5);
        assertThat(karen.getHp()).isZero();
        assertThat(karen.isAlive()).isFalse();
    }

    @Test
    void negativeDamageIsRejected() {
        Combatant karen = new Combatant("Karen Doge", STATS);

        assertThatThrownBy(() -> karen.takeDamage(-3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(karen.getHp()).isEqualTo(30);
    }
}
