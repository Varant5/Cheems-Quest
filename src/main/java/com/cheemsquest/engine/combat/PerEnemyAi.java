package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class PerEnemyAi implements EnemyAi {

    private final Map<Combatant, EnemyAi> ais = new IdentityHashMap<>();

    public PerEnemyAi assign(Combatant enemy, EnemyAi ai) {
        ais.put(enemy, ai);
        return this;
    }

    @Override
    public Skill chooseSkill(Combatant enemy, Combatant target, RandomGenerator rng) {
        return aiOf(enemy).chooseSkill(enemy, target, rng);
    }

    @Override
    public Optional<String> phaseShift(Combatant enemy, int hpAtRoundStart) {
        return aiOf(enemy).phaseShift(enemy, hpAtRoundStart);
    }

    private EnemyAi aiOf(Combatant enemy) {
        EnemyAi ai = ais.get(enemy);
        if (ai == null) {
            throw new IllegalArgumentException("No AI assigned to " + enemy.getName());
        }
        return ai;
    }
}
