package com.cheemsquest.domain;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.Rarity;
import com.cheemsquest.engine.model.StatBonus;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.InventorySlot;
import com.cheemsquest.persistence.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record InventoryView(List<ItemView> items, int gold, int hp, int maxHp, int attack, int defense) {

    public record ItemView(
            UUID id,
            String code,
            String name,
            String description,
            EquipmentSlot slot,
            Rarity rarity,
            StatBonus bonus,
            int sellValue,
            int quantity,
            boolean equipped) {

        static ItemView of(List<InventorySlot> copies) {
            InventorySlot first = copies.getFirst();
            Item item = first.getItem();
            return new ItemView(first.getId(), item.getCode(), item.getName(), item.getDescription(),
                    item.getSlot(), item.getRarity(), item.bonus(), item.getSellValue(), copies.size(),
                    first.isEquipped());
        }
    }

    private record StackKey(String code, boolean equipped) {
    }

    static InventoryView from(GameCharacter character) {
        Map<StackKey, List<InventorySlot>> stacks = new LinkedHashMap<>();
        for (InventorySlot slot : character.getInventory()) {
            stacks.computeIfAbsent(new StackKey(slot.getItem().getCode(), slot.isEquipped()), key -> new ArrayList<>())
                    .add(slot);
        }
        List<ItemView> items = stacks.values().stream().map(ItemView::of).toList();
        Stats stats = character.stats();
        return new InventoryView(items, character.getGold(), character.getCurrentHp(), stats.maxHp(), stats.attack(),
                stats.defense());
    }
}
