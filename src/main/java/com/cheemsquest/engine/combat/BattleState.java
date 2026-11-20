package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;

sealed interface BattleState permits PlayerTurn, EnemyTurn, Victory, Defeat {

    BattleStatus status();

    default BattleState onPlayerAction(Battle battle, Skill skill, Combatant target) {
        throw new IllegalStateException("Player cannot act during " + status());
    }

    default BattleState advance(Battle battle) {
        throw new IllegalStateException("Nothing to advance during " + status());
    }
}
