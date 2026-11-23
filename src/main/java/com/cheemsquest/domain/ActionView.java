package com.cheemsquest.domain;

import com.cheemsquest.engine.combat.BattleStatus;

import java.util.List;

public record ActionView(List<String> log, BattleStatus status, int hp, int maxHp, EncounterView encounter) {
}
