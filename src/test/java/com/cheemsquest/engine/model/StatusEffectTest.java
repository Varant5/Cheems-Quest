package com.cheemsquest.engine.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusEffectTest {

    private final Combatant cheems = new Combatant("Cheems", new Stats(50, 10, 2));

    @Test
    void poisonHurtsAtTheStartOfEachTurnUntilItWearsOff() {
        cheems.afflict(StatusEffect.poison(2, 3));

        assertThat(cheems.startTurn()).containsExactly(new StatusTick(cheems, StatusType.POISON, 3));
        assertThat(cheems.getStatuses()).containsExactly(StatusEffect.poison(1, 3));
        assertThat(cheems.startTurn()).containsExactly(new StatusTick(cheems, StatusType.POISON, 3));

        assertThat(cheems.has(StatusType.POISON)).isFalse();
        assertThat(cheems.startTurn()).isEmpty();
        assertThat(cheems.getHp()).isEqualTo(44);
    }

    @Test
    void stunSkipsOneTurn() {
        cheems.afflict(StatusEffect.stun(1));

        StatusTick tick = cheems.startTurn().getFirst();

        assertThat(tick.skipsTurn()).isTrue();
        assertThat(cheems.has(StatusType.STUN)).isFalse();
    }

    @Test
    void poisonThatKnocksOutDoesNotAlsoStun() {
        Combatant weak = new Combatant("Lil Doge", new Stats(20, 5, 0), 2);
        weak.afflict(StatusEffect.poison(3, 5));
        weak.afflict(StatusEffect.stun(1));

        assertThat(weak.startTurn()).containsExactly(new StatusTick(weak, StatusType.POISON, 2));
        assertThat(weak.isAlive()).isFalse();
    }

    @Test
    void reapplyingKeepsTheLongerAndStrongerEffect() {
        cheems.afflict(StatusEffect.poison(3, 2));
        cheems.afflict(StatusEffect.poison(1, 4));

        assertThat(cheems.getStatuses()).containsExactly(StatusEffect.poison(3, 4));
    }

    @Test
    void rejectsInvalidEffects() {
        assertThatThrownBy(() -> StatusEffect.poison(0, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StatusEffect.poison(2, -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StatusEffect(null, 1, 0)).isInstanceOf(NullPointerException.class);
    }
}
