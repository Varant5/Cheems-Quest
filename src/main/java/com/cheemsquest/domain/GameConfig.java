package com.cheemsquest.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.random.RandomGenerator;

@Configuration(proxyBeanMethods = false)
class GameConfig {

    // java.util.Random is thread-safe, unlike most other RandomGenerator implementations.
    @Bean
    RandomGenerator gameRandom() {
        return new Random();
    }
}
