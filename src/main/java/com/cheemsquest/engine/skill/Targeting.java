package com.cheemsquest.engine.skill;

import java.util.List;

public enum Targeting {
    SINGLE,
    ALL_ENEMIES,
    SELF;

    void validate(List<?> targets) {
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("skill needs at least one target");
        }
        if (this != ALL_ENEMIES && targets.size() != 1) {
            throw new IllegalArgumentException(this + " skill got " + targets.size() + " targets");
        }
    }
}
