package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Skill;

import java.util.random.RandomGenerator;

@FunctionalInterface
public interface EnemyAi {

    Skill chooseSkill(Combatant enemy, Combatant target, RandomGenerator rng);
}
