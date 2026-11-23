package com.cheemsquest.engine.model;

public record StatBonus(int maxHp, int attack, int defense) {

    public static final StatBonus NONE = new StatBonus(0, 0, 0);

    public StatBonus plus(StatBonus other) {
        return new StatBonus(maxHp + other.maxHp, attack + other.attack, defense + other.defense);
    }

    public StatBonus times(int factor) {
        return new StatBonus(maxHp * factor, attack * factor, defense * factor);
    }
}
