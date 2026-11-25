package com.cheemsquest.engine.model;

import java.util.Objects;

public class Combatant {

    private final String name;
    private final Stats stats;
    private int hp;
    private boolean lucky;

    public Combatant(String name, Stats stats) {
        this(name, stats, stats.maxHp());
    }

    public Combatant(String name, Stats stats, int hp) {
        this.name = Objects.requireNonNull(name, "name");
        this.stats = Objects.requireNonNull(stats, "stats");
        if (hp < 0 || hp > stats.maxHp()) {
            throw new IllegalArgumentException("hp must be in [0, " + stats.maxHp() + "], got " + hp);
        }
        this.hp = hp;
    }

    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("damage cannot be negative, got " + amount);
        }
        int dealt = Math.min(amount, hp);
        hp -= dealt;
        return dealt;
    }

    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("healing cannot be negative, got " + amount);
        }
        if (!isAlive()) {
            throw new IllegalStateException(name + " is down and cannot be healed");
        }
        int healed = Math.min(amount, stats.maxHp() - hp);
        hp += healed;
        return healed;
    }

    public void bless() {
        lucky = true;
    }

    public boolean isLucky() {
        return lucky;
    }

    public boolean consumeLuck() {
        boolean wasLucky = lucky;
        lucky = false;
        return wasLucky;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }

    public int getHp() {
        return hp;
    }

    @Override
    public String toString() {
        return name + " (" + hp + "/" + stats.maxHp() + " HP)";
    }
}
