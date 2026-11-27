package com.cheemsquest.engine.model;

public record StatusTick(Combatant actor, StatusType type, int damage) implements BattleEvent {

    public boolean skipsTurn() {
        return type == StatusType.STUN;
    }
}
