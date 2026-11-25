package com.cheemsquest.domain;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.ItemEffect;
import com.cheemsquest.engine.item.Rarity;
import com.cheemsquest.engine.model.StatBonus;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.Item;

import java.util.List;

public record ShopView(String location, int gold, List<OfferView> offers) {

    public record OfferView(
            String code,
            String name,
            String description,
            EquipmentSlot slot,
            Rarity rarity,
            StatBonus bonus,
            ItemEffect effect,
            int effectPower,
            int price) {

        static OfferView from(Item item) {
            return new OfferView(item.getCode(), item.getName(), item.getDescription(), item.getSlot(),
                    item.getRarity(), item.bonus(), item.getEffect(), item.getEffectPower(), item.buyPrice().orElseThrow());
        }
    }

    static ShopView from(GameCharacter character) {
        List<OfferView> offers = character.getLocation().getShopItems().stream().map(OfferView::from).toList();
        return new ShopView(character.getLocation().getName(), character.getGold(), offers);
    }
}
