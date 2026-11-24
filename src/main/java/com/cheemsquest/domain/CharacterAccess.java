package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("characterAccess")
@RequiredArgsConstructor
public class CharacterAccess {

    private final GameCharacterRepository characters;

    @Transactional(readOnly = true)
    public boolean isOwner(String playerId, UUID characterId) {
        UUID ownerId = characters.findOwnerId(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
        return ownerId.toString().equals(playerId);
    }
}
