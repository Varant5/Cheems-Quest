package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;

import java.util.List;
import java.util.random.RandomGenerator;

public class Bite implements Skill {

    static final double MULTIPLIER = 1.6;
    static final double MISS_CHANCE = 0.30;

    @Override
    public String name() {
        return "Bite";
    }

    @Override
    public Targeting targeting() {
        return Targeting.SINGLE;
    }

    @Override
    public SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng) {
        targeting().validate(targets);
        Combatant target = targets.getFirst();

        if (rng.nextDouble() < MISS_CHANCE) {
            return new SkillResult(name(), user, List.of(Hit.miss(target)));
        }
        int damage = DamageFormula.compute(user.getStats().attack(), MULTIPLIER, target.getStats().defense(), rng);
        int dealt = target.takeDamage(damage);
        return new SkillResult(name(), user, List.of(new Hit(target, dealt, HitOutcome.HIT)));
    }
}
