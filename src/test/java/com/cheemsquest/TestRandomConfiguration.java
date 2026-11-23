package com.cheemsquest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.random.RandomGenerator;

@TestConfiguration(proxyBeanMethods = false)
public class TestRandomConfiguration {

    // 0.9: no encounters except in locations with 100% chance, no crits, no misses.
    public static final double DEFAULT_ROLL = 0.9;

    @Bean
    @Primary
    SettableRandom settableRandom() {
        return new SettableRandom();
    }

    public static final class SettableRandom implements RandomGenerator {

        private volatile double value = DEFAULT_ROLL;

        public void set(double value) {
            this.value = value;
        }

        public void reset() {
            set(DEFAULT_ROLL);
        }

        @Override
        public double nextDouble() {
            return value;
        }

        @Override
        public long nextLong() {
            throw new UnsupportedOperationException("use nextDouble()");
        }
    }
}
