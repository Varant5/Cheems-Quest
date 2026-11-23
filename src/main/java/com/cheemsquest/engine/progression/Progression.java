package com.cheemsquest.engine.progression;

import com.cheemsquest.engine.model.StatBonus;

public final class Progression {

    public static final StatBonus LEVEL_UP_BONUS = new StatBonus(8, 2, 1);

    private Progression() {
    }

    public static int experienceForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be at least 1, got " + level);
        }
        return 25 * level * (level - 1);
    }

    public static int levelFor(int experience) {
        if (experience < 0) {
            throw new IllegalArgumentException("experience cannot be negative, got " + experience);
        }
        int level = 1;
        while (experienceForLevel(level + 1) <= experience) {
            level++;
        }
        return level;
    }
}
