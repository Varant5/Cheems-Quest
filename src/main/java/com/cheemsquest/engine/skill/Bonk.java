package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;

import java.util.List;
import java.util.random.RandomGenerator;

public class Bonk implements Skill {

    static final double MULTIPLIER = 1.0;
    static final double CRIT_CHANCE = 0.10;

    @Override
    public String name() {
        return "Bonk";
    }

    @Override
    public Targeting targeting() {
        return Targeting.SINGLE;
    }

    @Override
    public SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng) {
        targeting().validate(targets);
        Combatant target = targets.getFirst();

        boolean crit = user.consumeLuck() || rng.nextDouble() < CRIT_CHANCE;
        double multiplier = crit ? MULTIPLIER * DamageFormula.CRIT_MULTIPLIER : MULTIPLIER;
        int damage = DamageFormula.compute(user.getStats().attack(), multiplier, target.getStats().defense(), rng);

        int dealt = target.takeDamage(damage);
        HitOutcome outcome = crit ? HitOutcome.CRITICAL : HitOutcome.HIT;
        return new SkillResult(name(), user, List.of(new Hit(target, dealt, outcome)));
    }
}
