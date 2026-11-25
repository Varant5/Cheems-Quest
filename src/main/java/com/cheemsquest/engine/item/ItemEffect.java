package com.cheemsquest.engine.item;

import com.cheemsquest.engine.skill.Skill;

import java.util.Optional;

public enum ItemEffect {
    HEAL,
    PERMANENT_ATTACK,
    GUARANTEED_CRIT;

    public Optional<Skill> inBattle(String itemName, int power) {
        return switch (this) {
            case HEAL -> Optional.of(new HealingItem(itemName, power));
            case GUARANTEED_CRIT -> Optional.of(new LuckyCharm(itemName));
            case PERMANENT_ATTACK -> Optional.empty();
        };
    }
}
