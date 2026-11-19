package com.cheemsquest.engine.model;

public record Stats(int maxHp, int attack, int defense) {

    public Stats {
        if (maxHp <= 0) {
            throw new IllegalArgumentException("maxHp must be positive, got " + maxHp);
        }
        if (attack < 0 || defense < 0) {
            throw new IllegalArgumentException("attack and defense cannot be negative");
        }
    }
}
