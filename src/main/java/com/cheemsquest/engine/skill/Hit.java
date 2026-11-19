package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;

public record Hit(Combatant target, int damage, HitOutcome outcome) {

    public static Hit miss(Combatant target) {
        return new Hit(target, 0, HitOutcome.MISS);
    }
}
