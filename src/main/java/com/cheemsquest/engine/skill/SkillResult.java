package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;

import java.util.List;

public record SkillResult(String skillName, Combatant user, List<Hit> hits) {

    public SkillResult {
        hits = List.copyOf(hits);
    }

    public int totalDamage() {
        return hits.stream().mapToInt(Hit::damage).sum();
    }
}
