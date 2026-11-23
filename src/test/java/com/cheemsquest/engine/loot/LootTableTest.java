package com.cheemsquest.engine.loot;

import com.cheemsquest.engine.ConstantRandom;
import com.cheemsquest.engine.FixedRandom;
import com.cheemsquest.engine.random.WeightedTable;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class LootTableTest {

    private final WeightedTable<String> drops = WeightedTable.<String>builder()
            .add("wig", 3)
            .add("collar", 1)
            .build();

    @Test
    void dropsNothingWhenChanceRollFails() {
        LootTable<String> loot = LootTable.of(0.5, drops);

        assertThat(loot.roll(FixedRandom.of(0.5))).isEmpty();
    }

    @Test
    void picksWeightedItemWhenChanceRollSucceeds() {
        LootTable<String> loot = LootTable.of(0.5, drops);

        assertThat(loot.roll(FixedRandom.of(0.1, 0.0))).contains("wig");
        assertThat(loot.roll(FixedRandom.of(0.1, 0.8))).contains("collar");
    }

    @Test
    void guaranteedDropAlwaysDrops() {
        LootTable<String> loot = LootTable.of(1.0, drops);

        assertThat(loot.roll(new ConstantRandom(Math.nextDown(1.0)))).contains("collar");
    }

    @Test
    void emptyTableNeverDrops() {
        LootTable<String> loot = LootTable.empty();

        assertThat(loot.roll(new ConstantRandom(0.0))).isEmpty();
        assertThat(loot.dropChance()).isZero();
    }

    @Test
    void dropRateMatchesChance() {
        LootTable<String> loot = LootTable.of(0.25, drops);
        Random seeded = new Random(7);
        int samples = 20_000;

        long dropped = IntStream.range(0, samples).filter(i -> loot.roll(seeded).isPresent()).count();

        assertThat(dropped / (double) samples).isCloseTo(0.25, within(0.02));
    }

    @Test
    void rejectsChanceOutsideOfRange() {
        assertThatThrownBy(() -> LootTable.of(1.5, drops)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LootTable.of(-0.1, drops)).isInstanceOf(IllegalArgumentException.class);
    }
}
