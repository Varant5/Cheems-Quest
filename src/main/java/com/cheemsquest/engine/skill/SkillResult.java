package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.BattleEvent;
import com.cheemsquest.engine.model.Combatant;

import java.util.List;

public record SkillResult(String skillName, Combatant user, List<Hit> hits) implements BattleEvent {

    public SkillResult {
        hits = List.copyOf(hits);
    }

    @Override
    public Combatant actor() {
        return user;
    }

    public int totalDamage() {
        return hits.stream().filter(hit -> hit.outcome().dealsDamage()).mapToInt(Hit::damage).sum();
    }
}
