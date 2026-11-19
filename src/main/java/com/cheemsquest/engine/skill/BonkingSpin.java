package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;

import java.util.List;
import java.util.random.RandomGenerator;

public class BonkingSpin implements Skill {

    static final double MULTIPLIER = 0.6;

    @Override
    public String name() {
        return "Bonking Spin";
    }

    @Override
    public Targeting targeting() {
        return Targeting.ALL_ENEMIES;
    }

    @Override
    public SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng) {
        targeting().validate(targets);

        List<Hit> hits = targets.stream()
                .filter(Combatant::isAlive)
                .map(target -> {
                    int damage = DamageFormula.compute(
                            user.getStats().attack(), MULTIPLIER, target.getStats().defense(), rng);
                    return new Hit(target, target.takeDamage(damage), HitOutcome.HIT);
                })
                .toList();
        return new SkillResult(name(), user, hits);
    }
}
