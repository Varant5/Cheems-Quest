package com.cheemsquest.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.random.RandomGenerator;

public final class FixedRandom implements RandomGenerator {

    private final Deque<Double> values = new ArrayDeque<>();

    public static FixedRandom of(double... values) {
        FixedRandom rng = new FixedRandom();
        for (double v : values) {
            rng.values.add(v);
        }
        return rng;
    }

    @Override
    public double nextDouble() {
        if (values.isEmpty()) {
            throw new IllegalStateException("FixedRandom ran out of values");
        }
        return values.poll();
    }

    @Override
    public long nextLong() {
        throw new UnsupportedOperationException("use nextDouble()");
    }
}
