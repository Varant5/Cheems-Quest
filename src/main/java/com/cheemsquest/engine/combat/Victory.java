package com.cheemsquest.engine.combat;

record Victory() implements BattleState {

    @Override
    public BattleStatus status() {
        return BattleStatus.VICTORY;
    }
}
