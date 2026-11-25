package com.cheemsquest.engine.loot;

import com.cheemsquest.engine.ConstantRandom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldDropTest {

    private final GoldDrop karen = new GoldDrop(5, 10);

    @Test
    void rollCoversTheWholeRange() {
        assertThat(karen.roll(new ConstantRandom(0.0))).isEqualTo(5);
        assertThat(karen.roll(new ConstantRandom(0.5))).isEqualTo(8);
        assertThat(karen.roll(new ConstantRandom(0.999))).isEqualTo(10);
    }

    @Test
    void noGoldNeverRolls() {
        assertThat(GoldDrop.NONE.roll(new ConstantRandom(0.9))).isZero();
    }

    @Test
    void rejectsInvalidRanges() {
        assertThatThrownBy(() -> new GoldDrop(-1, 3)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GoldDrop(5, 4)).isInstanceOf(IllegalArgumentException.class);
    }
}
