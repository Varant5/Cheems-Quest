package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusTick;
import com.cheemsquest.engine.skill.Skill;

import java.util.List;

record PlayerTurn() implements BattleState {

    @Override
    public BattleStatus status() {
        return BattleStatus.PLAYER_TURN;
    }

    @Override
    public BattleState onPlayerAction(Battle battle, Skill skill, Combatant target) {
        List<Combatant> targets = battle.resolveTargets(skill, target);
        Combatant player = battle.getPlayer();

        List<StatusTick> ticks = player.startTurn();
        ticks.forEach(battle::record);
        if (!player.isAlive()) {
            return new Defeat();
        }
        if (ticks.stream().noneMatch(StatusTick::skipsTurn)) {
            battle.record(skill.use(player, targets, battle.rng()));
        }
        return battle.livingEnemies().isEmpty() ? new Victory() : new EnemyTurn();
    }
}
