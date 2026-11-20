package com.cheemsquest.engine.combat;

public enum BattleStatus {
    PLAYER_TURN,
    ENEMY_TURN,
    VICTORY,
    DEFEAT;

    public boolean isOver() {
        return this == VICTORY || this == DEFEAT;
    }
}
