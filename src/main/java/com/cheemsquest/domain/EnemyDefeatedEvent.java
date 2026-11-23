package com.cheemsquest.domain;

import java.util.UUID;

public record EnemyDefeatedEvent(UUID characterId, String enemyCode, String enemyName, int expReward, GameLog log) {
}
