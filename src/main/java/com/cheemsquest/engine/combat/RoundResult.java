package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.BattleEvent;
import com.cheemsquest.engine.skill.SkillResult;

import java.util.List;

public record RoundResult(List<BattleEvent> actions, BattleStatus status) {

    public RoundResult {
        actions = List.copyOf(actions);
    }

    public List<SkillResult> skillResults() {
        return actions.stream()
                .filter(SkillResult.class::isInstance)
                .map(SkillResult.class::cast)
                .toList();
    }
}
