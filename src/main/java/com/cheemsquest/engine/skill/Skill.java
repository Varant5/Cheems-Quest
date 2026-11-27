package com.cheemsquest.engine.skill;

import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusType;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public interface Skill {

    String name();

    Targeting targeting();

    /**
     *
     * @param targets {@link Targeting#SINGLE},
     *                {@link Targeting#ALL_ENEMIES}
     */
    SkillResult use(Combatant user, List<Combatant> targets, RandomGenerator rng);

    default Optional<StatusType> inflicts() {
        return Optional.empty();
    }

    default boolean finisher() {
        return false;
    }
}
