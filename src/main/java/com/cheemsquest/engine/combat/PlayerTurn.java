package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;

import java.util.List;

record PlayerTurn() implements BattleState {

    @Override
    public BattleStatus status() {
        return BattleStatus.PLAYER_TURN;
    }

    @Override
    public BattleState onPlayerAction(Battle battle, Skill skill, Combatant target) {
        List<Combatant> targets = battle.resolveTargets(skill, target);
        SkillResult result = skill.use(battle.getPlayer(), targets, battle.rng());
        battle.record(result);
        return battle.livingEnemies().isEmpty() ? new Victory() : new EnemyTurn();
    }
}
