package com.cheemsquest.domain;

import java.util.UUID;

public record LocationEnteredEvent(UUID characterId, String locationCode, GameLog log) {
}
