package com.cheemsquest.domain;

import java.util.List;

public record MoveView(String message, LocationView location, int hp, int maxHp, EncounterView encounter,
                       List<String> log) {
}
