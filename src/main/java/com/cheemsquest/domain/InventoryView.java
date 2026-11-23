package com.cheemsquest.domain;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.Rarity;
import com.cheemsquest.engine.model.StatBonus;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.InventorySlot;
import com.cheemsquest.persistence.Item;

import java.util.List;
import java.util.UUID;

public record InventoryView(List<ItemView> items, int hp, int maxHp, int attack, int defense) {

    public record ItemView(
            UUID id,
            String code,
            String name,
            String description,
            EquipmentSlot slot,
            Rarity rarity,
            StatBonus bonus,
            boolean equipped) {

        static ItemView from(InventorySlot inventorySlot) {
            Item item = inventorySlot.getItem();
            return new ItemView(inventorySlot.getId(), item.getCode(), item.getName(), item.getDescription(),
                    item.getSlot(), item.getRarity(), item.bonus(), inventorySlot.isEquipped());
        }
    }

    static InventoryView from(GameCharacter character) {
        Stats stats = character.stats();
        List<ItemView> items = character.getInventory().stream().map(ItemView::from).toList();
        return new InventoryView(items, character.getCurrentHp(), stats.maxHp(), stats.attack(), stats.defense());
    }
}
