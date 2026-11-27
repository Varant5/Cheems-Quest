package com.cheemsquest.engine.combat;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Skill;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class WeightedEnemyAi implements EnemyAi {

    private final WeightedTable<Skill> skills;

    public WeightedEnemyAi(WeightedTable<Skill> skills) {
        this.skills = Objects.requireNonNull(skills, "skills");
    }

    public static WeightedEnemyAi uniform(Skill... skills) {
        return new WeightedEnemyAi(WeightedTable.uniform(List.of(skills)));
    }

    @Override
    public Skill chooseSkill(Combatant enemy, Combatant target, RandomGenerator rng) {
        return skills.pick(rng);
    }
}
