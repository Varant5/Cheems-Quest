package com.cheemsquest.engine.skill;

public enum HitOutcome {
    HIT,
    CRITICAL,
    MISS,
    HEAL,
    LUCK;

    public boolean dealsDamage() {
        return this == HIT || this == CRITICAL;
    }
}
