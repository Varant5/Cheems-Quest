package com.cheemsquest.domain;

import com.cheemsquest.engine.item.ItemEffect;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.InventorySlot;
import com.cheemsquest.persistence.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String OUTFIT_MID_FIGHT = "No time to change outfits mid-fight. Such danger!";
    private static final String SHOPPING_MID_FIGHT = "No time for selling stuff mid-fight. Such danger!";
    private static final String ITEM_MID_FIGHT = "Mid-fight? Use it as your fight action instead.";

    private final GameCharacterRepository characters;
    private final EncounterService encounterService;

    @Transactional(readOnly = true)
    public InventoryView get(UUID characterId) {
        return InventoryView.from(load(characterId));
    }

    @Transactional
    public InventoryView equip(UUID characterId, UUID slotId) {
        GameCharacter character = loadOutsideOfFight(characterId, OUTFIT_MID_FIGHT);
        InventorySlot slot = slot(character, slotId);
        if (!slot.getItem().isEquipment()) {
            throw new GameRuleException("Cannot wear " + slot.getItem().getName() + ". Try using it instead.");
        }
        character.equip(slot);
        return InventoryView.from(character);
    }

    @Transactional
    public InventoryView use(UUID characterId, UUID slotId) {
        GameCharacter character = loadOutsideOfFight(characterId, ITEM_MID_FIGHT);
        InventorySlot slot = slot(character, slotId);
        Item item = slot.getItem();
        if (!item.isConsumable()) {
            throw new GameRuleException(item.getName() + " is not something you can use. Try equipping it.");
        }
        if (item.getEffect() == ItemEffect.GUARANTEED_CRIT) {
            throw new GameRuleException(item.getName() + " only works in a fight. Save it for a bonk.");
        }
        if (item.getEffect() == ItemEffect.HEAL && character.getCurrentHp() >= character.stats().maxHp()) {
            throw new GameRuleException("Already at full HP. Save the " + item.getName() + " for later.");
        }
        character.use(slot);
        return InventoryView.from(character);
    }

    @Transactional
    public InventoryView unequip(UUID characterId, UUID slotId) {
        GameCharacter character = loadOutsideOfFight(characterId, OUTFIT_MID_FIGHT);
        character.unequip(slot(character, slotId));
        return InventoryView.from(character);
    }

    @Transactional
    public InventoryView sell(UUID characterId, String itemCode, int quantity) {
        GameCharacter character = loadOutsideOfFight(characterId, SHOPPING_MID_FIGHT);
        if (character.getInventory().stream().noneMatch(slot -> slot.getItem().getCode().equals(itemCode))) {
            throw new NotFoundException("No " + itemCode + " in " + character.getName() + "'s inventory");
        }
        int available = character.unequippedCopies(itemCode).size();
        if (available == 0) {
            throw new GameRuleException("Cannot sell what you are wearing. Unequip it first.");
        }
        if (quantity > available) {
            throw new GameRuleException("Only " + available + " to sell, not " + quantity + ". Much math.");
        }
        character.sell(itemCode, quantity);
        return InventoryView.from(character);
    }

    private GameCharacter loadOutsideOfFight(UUID characterId, String message) {
        GameCharacter character = load(characterId);
        if (encounterService.findActive(characterId).isPresent()) {
            throw new GameRuleException(message);
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
