package com.cheemsquest.persistence;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.item.ItemEffect;
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
    private static final Item BURGER = Item.consumable("CHEEMSBURGER", "Cheemsburger", Rarity.COMMON, ItemEffect.HEAL,
            30, 5, 15);
    private static final Item COIN = Item.consumable("DOGE_COIN", "Doge Coin", Rarity.LEGENDARY,
            ItemEffect.PERMANENT_ATTACK, 10, 200, 500);
    private static final Item DICE = Item.consumable("LUCKY_DICE", "Lucky Dice", Rarity.RARE,
            ItemEffect.GUARANTEED_CRIT, 0, 15, 40);

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

    @Test
    void buyingPaysGoldAndAddsCopies() {
        cheems.addGold(50);

        int paid = cheems.buy(BURGER, 3);

        assertThat(paid).isEqualTo(45);
        assertThat(cheems.getGold()).isEqualTo(5);
        assertThat(cheems.getInventory()).hasSize(3).allSatisfy(slot -> assertThat(slot.getItem()).isEqualTo(BURGER));
    }

    @Test
    void cannotBuyWithoutGoldOrItemsNotForSale() {
        cheems.addGold(14);

        assertThatThrownBy(() -> cheems.buy(BURGER, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.buy(STICK, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.addGold(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(cheems.getGold()).isEqualTo(14);
        assertThat(cheems.getInventory()).isEmpty();
    }

    @Test
    void burgerHealsUpToMaxHpAndIsConsumed() {
        cheems.updateHp(50);
        InventorySlot burger = cheems.addItem(BURGER);

        int healed = cheems.use(burger);

        assertThat(healed).isEqualTo(15);
        assertThat(cheems.getCurrentHp()).isEqualTo(65);
        assertThat(cheems.getInventory()).isEmpty();
    }

    @Test
    void dogeCoinRaisesBaseAttackForever() {
        cheems.use(cheems.addItem(COIN));

        assertThat(cheems.baseStats().attack()).isEqualTo(24);
        assertThat(cheems.getInventory()).isEmpty();
    }

    @Test
    void consumablesCannotBeWornAndGearCannotBeEaten() {
        InventorySlot burger = cheems.addItem(BURGER);
        InventorySlot dice = cheems.addItem(DICE);
        InventorySlot stick = cheems.addItem(STICK);

        assertThatThrownBy(() -> cheems.equip(burger)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.use(stick)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cheems.use(dice)).isInstanceOf(IllegalArgumentException.class);
        assertThat(cheems.getInventory()).hasSize(3);
    }
}
