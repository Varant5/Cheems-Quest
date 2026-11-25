package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final GameCharacterRepository characters;
    private final EncounterService encounterService;

    @Transactional(readOnly = true)
    public ShopView get(UUID characterId) {
        return ShopView.from(loadAtShop(characterId));
    }

    @Transactional
    public InventoryView buy(UUID characterId, String itemCode, int quantity) {
        GameCharacter character = loadAtShop(characterId);
        Item item = character.getLocation().shopItem(itemCode)
                .orElseThrow(() -> new NotFoundException("The shop does not sell " + itemCode));
        int total = item.buyPrice().orElseThrow() * quantity;
        if (total > character.getGold()) {
            throw new GameRuleException("Need " + total + " gold, have " + character.getGold() + ". Much poor.");
        }
        character.buy(item, quantity);
        characters.flush();
        return InventoryView.from(character);
    }

    private GameCharacter loadAtShop(UUID characterId) {
        GameCharacter character = characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
        if (!character.getLocation().hasShop()) {
            throw new GameRuleException("No shop in " + character.getLocation().getName() + ". Such empty.");
        }
        if (encounterService.findActive(characterId).isPresent()) {
            throw new GameRuleException("No time for shopping mid-fight. Such danger!");
        }
        return character;
    }
}
