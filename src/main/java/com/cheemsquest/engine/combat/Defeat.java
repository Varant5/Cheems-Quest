package com.cheemsquest.engine.combat;

record Defeat() implements BattleState {

    @Override
    public BattleStatus status() {
        return BattleStatus.DEFEAT;
    }
}
