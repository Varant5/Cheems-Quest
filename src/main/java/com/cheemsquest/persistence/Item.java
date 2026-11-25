package com.cheemsquest.persistence;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.ItemEffect;
import com.cheemsquest.engine.item.Rarity;
import com.cheemsquest.engine.model.StatBonus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Optional;

@Entity
@Table(name = "item")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Rarity rarity;

    @Column(name = "bonus_max_hp", nullable = false)
    private int bonusMaxHp;

    @Column(name = "bonus_attack", nullable = false)
    private int bonusAttack;

    @Column(name = "bonus_defense", nullable = false)
    private int bonusDefense;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ItemEffect effect;

    @Column(name = "effect_power", nullable = false)
    private int effectPower;

    @Column(name = "sell_value", nullable = false)
    private int sellValue;

    @Getter(AccessLevel.NONE)
    @Column(name = "buy_price")
    private Integer buyPrice;

    public Item(String code, String name, String description, EquipmentSlot slot, Rarity rarity, StatBonus bonus,
                int sellValue) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.slot = slot;
        this.rarity = rarity;
        this.bonusMaxHp = bonus.maxHp();
        this.bonusAttack = bonus.attack();
        this.bonusDefense = bonus.defense();
        this.sellValue = sellValue;
    }

    public static Item consumable(String code, String name, Rarity rarity, ItemEffect effect, int effectPower,
                                  int sellValue, int buyPrice) {
        Item item = new Item(code, name, "", null, rarity, StatBonus.NONE, sellValue);
        item.effect = effect;
        item.effectPower = effectPower;
        item.buyPrice = buyPrice;
        return item;
    }

    public StatBonus bonus() {
        return new StatBonus(bonusMaxHp, bonusAttack, bonusDefense);
    }

    public boolean isEquipment() {
        return slot != null;
    }

    public boolean isConsumable() {
        return effect != null;
    }

    public Optional<Integer> buyPrice() {
        return Optional.ofNullable(buyPrice);
    }
}
