package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.InventorySlot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final GameCharacterRepository characters;
    private final EncounterService encounterService;

    @Transactional(readOnly = true)
    public InventoryView get(UUID characterId) {
        return InventoryView.from(load(characterId));
    }

    @Transactional
    public InventoryView equip(UUID characterId, UUID slotId) {
        GameCharacter character = loadOutsideOfFight(characterId);
        character.equip(slot(character, slotId));
        return InventoryView.from(character);
    }

    @Transactional
    public InventoryView unequip(UUID characterId, UUID slotId) {
        GameCharacter character = loadOutsideOfFight(characterId);
        character.unequip(slot(character, slotId));
        return InventoryView.from(character);
    }

    private GameCharacter loadOutsideOfFight(UUID characterId) {
        GameCharacter character = load(characterId);
        if (encounterService.findActive(characterId).isPresent()) {
            throw new GameRuleException("No time to change outfits mid-fight. Such danger!");
        }
        return character;
    }

    private GameCharacter load(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }

    private static InventorySlot slot(GameCharacter character, UUID slotId) {
        return character.inventorySlot(slotId)
                .orElseThrow(() -> new NotFoundException("Item " + slotId + " is not in " + character.getName() + "'s inventory"));
    }
}
