package com.cheemsquest.persistence;

import com.cheemsquest.engine.item.EquipmentSlot;
import com.cheemsquest.engine.model.StatBonus;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.progression.Progression;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Not named "Character" to avoid clashing with java.lang.Character.
@Entity
@Table(name = "game_character")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int experience;

    @Getter(AccessLevel.NONE)
    @Column(name = "max_hp", nullable = false)
    private int baseMaxHp;

    @Getter(AccessLevel.NONE)
    @Column(name = "attack", nullable = false)
    private int baseAttack;

    @Getter(AccessLevel.NONE)
    @Column(name = "defense", nullable = false)
    private int baseDefense;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    @Column(nullable = false)
    private int gold;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_code")
    private Location location;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("acquiredAt")
    private List<InventorySlot> inventory = new ArrayList<>();

    public GameCharacter(Player player, String name, Stats stats, Location startLocation) {
        this.player = player;
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.baseMaxHp = stats.maxHp();
        this.baseAttack = stats.attack();
        this.baseDefense = stats.defense();
        this.currentHp = stats.maxHp();
        this.location = startLocation;
        this.createdAt = Instant.now();
    }

    public Stats baseStats() {
        return new Stats(baseMaxHp, baseAttack, baseDefense);
    }

    public StatBonus equipmentBonus() {
        return inventory.stream()
                .filter(InventorySlot::isEquipped)
                .map(slot -> slot.getItem().bonus())
                .reduce(StatBonus.NONE, StatBonus::plus);
    }

    public Stats stats() {
        return baseStats().plus(equipmentBonus());
    }

    public List<InventorySlot> getInventory() {
        return List.copyOf(inventory);
    }

    public Optional<InventorySlot> inventorySlot(UUID slotId) {
        return inventory.stream().filter(slot -> slotId.equals(slot.getId())).findFirst();
    }

    public InventorySlot addItem(Item item) {
        InventorySlot slot = new InventorySlot(this, item);
        inventory.add(slot);
        return slot;
    }

    public List<InventorySlot> unequippedCopies(String itemCode) {
        return inventory.stream()
                .filter(slot -> !slot.isEquipped() && slot.getItem().getCode().equals(itemCode))
                .toList();
    }

    public int sell(String itemCode, int quantity) {
        List<InventorySlot> copies = unequippedCopies(itemCode);
        if (quantity < 1 || quantity > copies.size()) {
            throw new IllegalArgumentException("cannot sell " + quantity + " of " + itemCode + ", "
                    + copies.size() + " unequipped");
        }
        List<InventorySlot> sold = copies.subList(copies.size() - quantity, copies.size());
        int earned = sold.getFirst().getItem().getSellValue() * quantity;
        inventory.removeAll(sold);
        gold += earned;
        return earned;
    }

    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("gold gain cannot be negative, got " + amount);
        }
        gold += amount;
    }

    public int buy(Item item, int quantity) {
        int price = item.buyPrice()
                .orElseThrow(() -> new IllegalArgumentException(item.getCode() + " is not for sale"));
        int total = price * quantity;
        if (quantity < 1 || total > gold) {
            throw new IllegalArgumentException("cannot buy " + quantity + " of " + item.getCode() + " with " + gold + " gold");
        }
        gold -= total;
        for (int i = 0; i < quantity; i++) {
            addItem(item);
        }
        return total;
    }

    public int use(InventorySlot slot) {
        requireOwned(slot);
        Item item = slot.getItem();
        if (!item.isConsumable()) {
            throw new IllegalArgumentException(item.getCode() + " is not consumable");
        }
        int effect = switch (item.getEffect()) {
            case HEAL -> {
                int before = currentHp;
                currentHp = Math.min(stats().maxHp(), currentHp + item.getEffectPower());
                yield currentHp - before;
            }
            case PERMANENT_ATTACK -> {
                baseAttack += item.getEffectPower();
                yield item.getEffectPower();
            }
            case GUARANTEED_CRIT -> throw new IllegalArgumentException(item.getCode() + " only works in a fight");
        };
        inventory.remove(slot);
        return effect;
    }

    public void equip(InventorySlot slot) {
        requireOwned(slot);
        if (!slot.getItem().isEquipment()) {
            throw new IllegalArgumentException(slot.getItem().getCode() + " cannot be equipped");
        }
        EquipmentSlot target = slot.getItem().getSlot();
        inventory.stream()
                .filter(other -> other != slot && other.isEquipped() && other.getItem().getSlot() == target)
                .forEach(InventorySlot::unequip);
        slot.equip();
        clampHp();
    }

    public void unequip(InventorySlot slot) {
        requireOwned(slot);
        slot.unequip();
        clampHp();
    }

    public void moveTo(Location destination) {
        this.location = destination;
    }

    public void updateHp(int hp) {
        int maxHp = stats().maxHp();
        if (hp < 0 || hp > maxHp) {
            throw new IllegalArgumentException("hp must be in [0, " + maxHp + "], got " + hp);
        }
        this.currentHp = hp;
    }

    public void restoreHp() {
        this.currentHp = stats().maxHp();
    }

    public void respawnAt(Location home) {
        moveTo(home);
        restoreHp();
    }

    public int gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("experience gain cannot be negative, got " + amount);
        }
        int previousLevel = level;
        experience += amount;
        level = Progression.levelFor(experience);

        int levelsGained = level - previousLevel;
        if (levelsGained > 0) {
            Stats grown = baseStats().plus(Progression.LEVEL_UP_BONUS.times(levelsGained));
            baseMaxHp = grown.maxHp();
            baseAttack = grown.attack();
            baseDefense = grown.defense();
            restoreHp();
        }
        return levelsGained;
    }

    public int experienceForNextLevel() {
        return Progression.experienceForLevel(level + 1);
    }

    private void clampHp() {
        currentHp = Math.min(currentHp, stats().maxHp());
    }

    private void requireOwned(InventorySlot slot) {
        if (!inventory.contains(slot)) {
            throw new IllegalArgumentException("item " + slot.getId() + " does not belong to " + name);
        }
    }
}
