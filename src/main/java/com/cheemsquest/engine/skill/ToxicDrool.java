package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusEffect;

import java.util.List;
import java.util.random.RandomGenerator;

public class ToxicDrool implements Skill {

    static final double MULTIPLIER = 0.6;
    static final int POISON_TURNS = 3;
    static final int MIN_POISON_DAMAGE = 2;

    @Override
    public String name() {
        return "Toxic Drool";
    }

    @Override
    public Targeting targeting() {
        return Targeting.SINGLE;
    }

    @Override
    public SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng) {
        targeting().validate(targets);
        Combatant target = targets.getFirst();

        boolean crit = user.consumeLuck();
        double multiplier = crit ? MULTIPLIER * DamageFormula.CRIT_MULTIPLIER : MULTIPLIER;
        int damage = DamageFormula.compute(user.getStats().attack(), multiplier, target.getStats().defense(), rng);
        int dealt = target.takeDamage(damage);
        HitOutcome outcome = crit ? HitOutcome.CRITICAL : HitOutcome.HIT;
        if (!target.isAlive()) {
            return new SkillResult(name(), user, List.of(new Hit(target, dealt, outcome)));
        }
        StatusEffect poison = StatusEffect.poison(POISON_TURNS,
                Math.max(MIN_POISON_DAMAGE, user.getStats().attack() / 4));
        target.afflict(poison);
        return new SkillResult(name(), user, List.of(new Hit(target, dealt, outcome, poison)));
    }
}
