package com.cheemsquest.domain;

import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.persistence.GameCharacter;

import java.util.UUID;

public record CharacterView(
        UUID id,
        String name,
        int level,
        int experience,
        int experienceForNextLevel,
        int hp,
        int maxHp,
        int attack,
        int defense,
        int gold,
        LocationView location) {

    static CharacterView from(GameCharacter character) {
        Stats stats = character.stats();
        return new CharacterView(
                character.getId(),
                character.getName(),
                character.getLevel(),
                character.getExperience(),
                character.experienceForNextLevel(),
                character.getCurrentHp(),
                stats.maxHp(),
                stats.attack(),
                stats.defense(),
                character.getGold(),
                LocationView.from(character.getLocation()));
    }
}
