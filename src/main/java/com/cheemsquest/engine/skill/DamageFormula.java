package com.cheemsquest.engine.skill;

import java.util.random.RandomGenerator;

final class DamageFormula {

    static final double VARIANCE = 0.10;

    private DamageFormula() {
    }

    static int compute(int attack, double multiplier, int defense, RandomGenerator rng) {
        double roll = 1.0 - VARIANCE + rng.nextDouble() * 2 * VARIANCE;
        long raw = Math.round(attack * multiplier * roll) - defense;
        return (int) Math.max(1, raw);
    }
}
