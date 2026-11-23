package com.cheemsquest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_slot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id")
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_code")
    private Item item;

    @Column(nullable = false)
    private boolean equipped;

    @Column(name = "acquired_at", nullable = false)
    private Instant acquiredAt;

    InventorySlot(GameCharacter character, Item item) {
        this.character = character;
        this.item = item;
        this.equipped = false;
        this.acquiredAt = Instant.now();
    }

    void equip() {
        this.equipped = true;
    }

    void unequip() {
        this.equipped = false;
    }
}
