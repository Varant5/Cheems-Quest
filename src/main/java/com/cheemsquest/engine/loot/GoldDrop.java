package com.cheemsquest.engine.loot;

import java.util.random.RandomGenerator;

public record GoldDrop(int min, int max) {

    public static final GoldDrop NONE = new GoldDrop(0, 0);

    public GoldDrop {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("gold range must satisfy 0 <= min <= max, got [" + min + ", " + max + "]");
        }
    }

    public int roll(RandomGenerator rng) {
        if (max == 0) {
            return 0;
        }
        return Math.min(max, min + (int) (rng.nextDouble() * (max - min + 1)));
    }
}
