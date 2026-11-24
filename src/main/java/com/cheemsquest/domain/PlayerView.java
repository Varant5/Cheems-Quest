package com.cheemsquest.domain;

import com.cheemsquest.persistence.Player;

import java.util.UUID;

public record PlayerView(UUID id, String username) {

    static PlayerView from(Player player) {
        return new PlayerView(player.getId(), player.getUsername());
    }
}
