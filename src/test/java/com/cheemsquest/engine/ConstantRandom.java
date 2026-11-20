package com.cheemsquest.engine;

import java.util.random.RandomGenerator;

public record ConstantRandom(double value) implements RandomGenerator {

    public static final ConstantRandom NEUTRAL = new ConstantRandom(0.5);

    @Override
    public double nextDouble() {
        return value;
    }

    @Override
    public long nextLong() {
        throw new UnsupportedOperationException("use nextDouble()");
    }
}
