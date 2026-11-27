package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusTick;
import com.cheemsquest.engine.skill.Skill;

import java.util.List;

record EnemyTurn() implements BattleState {

    @Override
    public BattleStatus status() {
        return BattleStatus.ENEMY_TURN;
    }

    @Override
    public BattleState advance(Battle battle) {
        Combatant player = battle.getPlayer();
        for (Combatant enemy : battle.livingEnemies()) {
            List<StatusTick> ticks = enemy.startTurn();
            ticks.forEach(battle::record);
            if (!enemy.isAlive() || ticks.stream().anyMatch(StatusTick::skipsTurn)) {
                continue;
            }
            Skill skill = battle.enemyAi().chooseSkill(enemy, battle.rng());
            battle.record(skill.use(enemy, List.of(player), battle.rng()));
            if (!player.isAlive()) {
                return new Defeat();
            }
        }
        return battle.livingEnemies().isEmpty() ? new Victory() : new PlayerTurn();
    }
}
