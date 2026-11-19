package com.cheemsquest.engine.skill;

import java.util.List;

public enum Targeting {
    SINGLE,
    ALL_ENEMIES;

    void validate(List<?> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("skill needs at least one target");
        }
        if (this == SINGLE && targets.size() != 1) {
            throw new IllegalArgumentException("single-target skill got " + targets.size() + " targets");
        }
    }
}
