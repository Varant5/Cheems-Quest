package com.cheemsquest.domain;

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
        LocationView location) {

    static CharacterView from(GameCharacter character) {
        return new CharacterView(
                character.getId(),
                character.getName(),
                character.getLevel(),
                character.getExperience(),
                character.experienceForNextLevel(),
                character.getCurrentHp(),
                character.getMaxHp(),
                character.getAttack(),
                character.getDefense(),
                LocationView.from(character.getLocation()));
    }
}
