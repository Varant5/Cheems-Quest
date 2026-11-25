package com.cheemsquest.engine.item;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.skill.Hit;
import com.cheemsquest.engine.skill.HitOutcome;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillResult;
import com.cheemsquest.engine.skill.Targeting;

import java.util.List;
import java.util.random.RandomGenerator;

record HealingItem(String name, int power) implements Skill {

    @Override
    public Targeting targeting() {
        return Targeting.SELF;
    }

    @Override
    public SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng) {
        return new SkillResult(name, user, List.of(new Hit(user, user.heal(power), HitOutcome.HEAL)));
    }
}
