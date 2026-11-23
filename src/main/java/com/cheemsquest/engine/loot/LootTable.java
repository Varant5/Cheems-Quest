package com.cheemsquest.engine.loot;

import com.cheemsquest.engine.random.WeightedTable;

import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class LootTable<T> {

    private static final LootTable<?> EMPTY = new LootTable<>(0.0, null);

    private final double dropChance;
    private final WeightedTable<T> drops;

    private LootTable(double dropChance, WeightedTable<T> drops) {
        if (dropChance < 0.0 || dropChance > 1.0) {
            throw new IllegalArgumentException("drop chance must be in [0, 1], got " + dropChance);
        }
        this.dropChance = dropChance;
        this.drops = drops;
    }

    public static <T> LootTable<T> of(double dropChance, WeightedTable<T> drops) {
        return new LootTable<>(dropChance, Objects.requireNonNull(drops, "drops"));
    }

    @SuppressWarnings("unchecked")
    public static <T> LootTable<T> empty() {
        return (LootTable<T>) EMPTY;
    }

    public Optional<T> roll(RandomGenerator rng) {
        if (drops == null || rng.nextDouble() >= dropChance) {
            return Optional.empty();
        }
        return Optional.of(drops.pick(rng));
    }

    public double dropChance() {
        return drops == null ? 0.0 : dropChance;
    }
}
