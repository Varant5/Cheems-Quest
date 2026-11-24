package com.cheemsquest.persistence;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.Rarity;
import com.cheemsquest.engine.model.StatBonus;
import com.cheemsquest.engine.model.Stats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameCharacterTest {

    private static final Item STICK = new Item("VERY_STICK", "Very Stick", "", EquipmentSlot.WEAPON, Rarity.COMMON,
            new StatBonus(0, 2, 0), 2);
    private static final Item BONE = new Item("LEGENDARY_BONE", "Legendary Bone", "", EquipmentSlot.WEAPON,
            Rarity.LEGENDARY, new StatBonus(0, 8, 0), 150);
    private static final Item HELMET = new Item("STONKS_HELMET", "Stonks Helmet", "", EquipmentSlot.HEAD, Rarity.EPIC,
            new StatBonus(10, 0, 3), 40);

    private final GameCharacter cheems = new GameCharacter(null, "Cheems", new Stats(65, 14, 3), null);

    @Test
    void experienceBelowThresholdKeepsLevel() {
        int levels = cheems.gainExperience(49);

        assertThat(levels).isZero();
        assertThat(cheems.getLevel()).isEqualTo(1);
        assertThat(cheems.getExperience()).isEqualTo(49);
        assertThat(cheems.experienceForNextLevel()).isEqualTo(50);
    }

    @Test
    void levelUpRaisesStatsAndRestoresHp() {
        cheems.updateHp(10);

        int levels = cheems.gainExperience(50);

        assertThat(levels).isEqualTo(1);
        assertThat(cheems.getLevel()).isEqualTo(2);
        assertThat(cheems.stats()).isEqualTo(new Stats(73, 16, 4));
        assertThat(cheems.getCurrentHp()).isEqualTo(73);
        assertThat(cheems.experienceForNextLevel()).isEqualTo(150);
    }

    @Test
    void bigRewardCanGrantSeveralLevels() {
        int levels = cheems.gainExperience(300);

        assertThat(levels).isEqualTo(3);
        assertThat(cheems.getLevel()).isEqualTo(4);
        assertThat(cheems.stats()).isEqualTo(new Stats(89, 20, 6));
    }

    @Test
    void newItemsAreNotEquipped() {
        cheems.addItem(STICK);

        assertThat(cheems.getInventory()).singleElement().satisfies(slot -> assertThat(slot.isEquipped()).isFalse());
        assertThat(cheems.stats()).isEqualTo(new Stats(65, 14, 3));
    }

    @Test
    void equippedItemsAddTheirBonuses() {
        cheems.equip(cheems.addItem(STICK));
        cheems.equip(cheems.addItem(HELMET));

        assertThat(cheems.stats()).isEqualTo(new Stats(75, 16, 6));
        assertThat(cheems.baseStats()).isEqualTo(new Stats(65, 14, 3));
    }

    @Test
    void equippingReplacesItemInTheSameSlot() {
        InventorySlot stick = cheems.addItem(STICK);
        InventorySlot bone = cheems.addItem(BONE);
        cheems.equip(stick);

        cheems.equip(bone);

        assertThat(stick.isEquipped()).isFalse();
        assertThat(bone.isEquipped()).isTrue();
        assertThat(cheems.stats().attack()).isEqualTo(22);
    }

    @Test
    void unequippingHpItemCapsCurrentHp() {
        cheems.equip(cheems.addItem(HELMET));
        cheems.restoreHp();
        assertThat(cheems.getCurrentHp()).isEqualTo(75);

        cheems.unequip(cheems.getInventory().getFirst());

        assertThat(cheems.getCurrentHp()).isEqualTo(65);
    }

    @Test
    void levelUpWithGearHealsToEffectiveMaxHp() {
        cheems.equip(cheems.addItem(HELMET));

        cheems.gainExperience(50);

        assertThat(cheems.baseStats().maxHp()).isEqualTo(73);
        assertThat(cheems.getCurrentHp()).isEqualTo(83);
    }

    @Test
    void cannotEquipSomeoneElsesItem() {
        GameCharacter doge = new GameCharacter(null, "Doge", new Stats(50, 10, 1), null);
        InventorySlot dogesStick = doge.addItem(STICK);

        assertThatThrownBy(() -> cheems.equip(dogesStick)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sellingRemovesUnequippedCopiesAndPaysGold() {
        InventorySlot worn = cheems.addItem(STICK);
        cheems.equip(worn);
        cheems.addItem(STICK);
        cheems.addItem(STICK);
        cheems.addItem(HELMET);

        int earned = cheems.sell("VERY_STICK", 2);

        assertThat(earned).isEqualTo(4);
        assertThat(cheems.getGold()).isEqualTo(4);
        assertThat(cheems.getInventory()).first().isSameAs(worn);
        assertThat(cheems.getInventory()).extracting(slot -> slot.getItem().getCode())
                .containsExactly("VERY_STICK", "STONKS_HELMET");
        assertThat(cheems.stats().attack()).isEqualTo(16);
    }

    @Test
    void cannotSellEquippedOrMissingCopies() {
        cheems.equip(cheems.addItem(STICK));
        cheems.addItem(STICK);

        assertThatThrownBy(() -> cheems.sell("VERY_STICK", 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.sell("VERY_STICK", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.sell("LEGENDARY_BONE", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(cheems.getInventory()).hasSize(2);
        assertThat(cheems.getGold()).isZero();
    }
}
