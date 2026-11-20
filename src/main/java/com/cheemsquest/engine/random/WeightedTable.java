package com.cheemsquest.engine.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class WeightedTable<T> {

    public record Entry<T>(T value, int weight) {

        public Entry {
            Objects.requireNonNull(value, "value");
            if (weight <= 0) {
                throw new IllegalArgumentException("weight must be positive, got " + weight + " for " + value);
            }
        }
    }

    private final List<Entry<T>> entries;
    private final int totalWeight;

    private WeightedTable(List<Entry<T>> entries) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("weighted table needs at least one entry");
        }
        this.entries = List.copyOf(entries);
        this.totalWeight = entries.stream().mapToInt(Entry::weight).sum();
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> WeightedTable<T> uniform(List<T> values) {
        Builder<T> builder = builder();
        values.forEach(value -> builder.add(value, 1));
        return builder.build();
    }

    public T pick(RandomGenerator rng) {
        double roll = rng.nextDouble() * totalWeight;
        int cumulative = 0;
        for (Entry<T> entry : entries) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                return entry.value();
            }
        }
        // Floating-point rounding can push roll to exactly totalWeight.
        return entries.getLast().value();
    }

    public List<Entry<T>> entries() {
        return entries;
    }

    public int totalWeight() {
        return totalWeight;
    }

    public static final class Builder<T> {

        private final List<Entry<T>> entries = new ArrayList<>();

        private Builder() {
        }

        public Builder<T> add(T value, int weight) {
            entries.add(new Entry<>(value, weight));
            return this;
        }

        public WeightedTable<T> build() {
            return new WeightedTable<>(entries);
        }
    }
}
