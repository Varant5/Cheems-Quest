package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.StatusType;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public class Headbutt implements Skill {

    static final double MULTIPLIER = 1.2;
    static final double STUN_CHANCE = 0.30;

    @Override
    public String name() {
        return "Headbutt";
    }

    @Override
    public Targeting targeting() {
        return Targeting.SINGLE;
    }

    @Override
    public Optional<StatusType> inflicts() {
        return Optional.of(StatusType.STUN);
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
        if (!target.isAlive() || rng.nextDouble() >= STUN_CHANCE) {
            return new SkillResult(name(), user, List.of(new Hit(target, dealt, outcome)));
        }
        StatusEffect stun = StatusEffect.stun(1);
        target.afflict(stun);
        return new SkillResult(name(), user, List.of(new Hit(target, dealt, outcome, stun)));
    }
}
