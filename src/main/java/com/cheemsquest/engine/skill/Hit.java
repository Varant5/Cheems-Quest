package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusEffect;

public record Hit(Combatant target, int damage, HitOutcome outcome, StatusEffect inflicted) {

    public Hit(Combatant target, int damage, HitOutcome outcome) {
        this(target, damage, outcome, null);
    }

    public static Hit miss(Combatant target) {
        return new Hit(target, 0, HitOutcome.MISS);
    }
}
