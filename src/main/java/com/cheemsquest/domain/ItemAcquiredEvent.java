package com.cheemsquest.domain;

import java.util.UUID;

public record ItemAcquiredEvent(UUID characterId, String itemCode, GameLog log) {
}
