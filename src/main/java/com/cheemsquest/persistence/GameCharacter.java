package com.cheemsquest.persistence;

import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.progression.Progression;
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

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private int attack;

    @Column(nullable = false)
    private int defense;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_code")
    private Location location;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GameCharacter(Player player, String name, Stats stats, Location startLocation) {
        this.player = player;
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.maxHp = stats.maxHp();
        this.attack = stats.attack();
        this.defense = stats.defense();
        this.currentHp = stats.maxHp();
        this.location = startLocation;
        this.createdAt = Instant.now();
    }

    public Stats stats() {
        return new Stats(maxHp, attack, defense);
    }

    public void moveTo(Location destination) {
        this.location = destination;
    }

    public void updateHp(int hp) {
        if (hp < 0 || hp > maxHp) {
            throw new IllegalArgumentException("hp must be in [0, " + maxHp + "], got " + hp);
        }
        this.currentHp = hp;
    }

    public void restoreHp() {
        this.currentHp = maxHp;
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
            Stats grown = stats().plus(Progression.LEVEL_UP_BONUS.times(levelsGained));
            maxHp = grown.maxHp();
            attack = grown.attack();
            defense = grown.defense();
            restoreHp();
        }
        return levelsGained;
    }

    public int experienceForNextLevel() {
        return Progression.experienceForLevel(level + 1);
    }
}
