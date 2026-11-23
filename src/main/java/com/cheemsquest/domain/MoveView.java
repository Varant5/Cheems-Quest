package com.cheemsquest.domain;

public record MoveView(String message, LocationView location, int hp, int maxHp, EncounterView encounter) {
}
