package com.cheemsquest.engine.model;

import java.util.Objects;

public record StatusEffect(StatusType type, int turns, int power) {

    public StatusEffect {
        Objects.requireNonNull(type, "type");
        if (turns <= 0) {
            throw new IllegalArgumentException("status must last at least one turn, got " + turns);
        }
        if (power < 0) {
            throw new IllegalArgumentException("status power cannot be negative, got " + power);
        }
    }

    public static StatusEffect poison(int turns, int damagePerTurn) {
        return new StatusEffect(StatusType.POISON, turns, damagePerTurn);
    }

    public static StatusEffect stun(int turns) {
        return new StatusEffect(StatusType.STUN, turns, 0);
    }

    StatusEffect strongest(StatusEffect other) {
        return new StatusEffect(type, Math.max(turns, other.turns), Math.max(power, other.power));
    }
}
