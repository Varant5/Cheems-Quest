package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.Targeting;

import java.util.Objects;
import java.util.random.RandomGenerator;

public final class TacticalEnemyAi implements EnemyAi {

    static final double FINISH_THRESHOLD = 0.3;
    static final int FINISHER_BONUS = 3;

    private final WeightedTable<Skill> skills;

    public TacticalEnemyAi(WeightedTable<Skill> skills) {
        this.skills = Objects.requireNonNull(skills, "skills");
    }

    @Override
    public Skill chooseSkill(Combatant enemy, Combatant target, RandomGenerator rng) {
        WeightedTable.Builder<Skill> adjusted = WeightedTable.builder();
        boolean anyLeft = false;
        for (WeightedTable.Entry<Skill> entry : skills.entries()) {
            int weight = weight(entry.value(), entry.weight(), target);
            if (weight > 0) {
                adjusted.add(entry.value(), weight);
                anyLeft = true;
            }
        }
        return anyLeft ? adjusted.build().pick(rng) : skills.pick(rng);
    }

    static int weight(Skill skill, int baseWeight, Combatant target) {
        if (skill.targeting() == Targeting.ALL_ENEMIES) {
            return 0;
        }
        if (skill.inflicts().filter(target::has).isPresent()) {
            return 0;
        }
        if (skill.finisher() && target.getHp() <= target.getStats().maxHp() * FINISH_THRESHOLD) {
            return baseWeight * FINISHER_BONUS;
        }
        return baseWeight;
    }
}
