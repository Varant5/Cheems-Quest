package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.skill.SkillResult;

import java.util.List;

public record RoundResult(List<SkillResult> actions, BattleStatus status) {

    public RoundResult {
        actions = List.copyOf(actions);
    }
}
