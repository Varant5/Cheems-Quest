package com.cheemsquest.persistence;

import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.engine.model.StatusEffect;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "encounter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Encounter {

    public static final int PLAYER = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id")
    private GameCharacter character;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BattleStatus status;

    @ElementCollection
    @CollectionTable(name = "encounter_enemy", joinColumns = @JoinColumn(name = "encounter_id"))
    @OrderColumn(name = "position")
    private List<EncounterEnemy> enemies = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @ElementCollection
    @CollectionTable(name = "encounter_status", joinColumns = @JoinColumn(name = "encounter_id"))
    private List<EncounterStatus> statuses = new ArrayList<>();

    @Column(name = "player_lucky", nullable = false)
    private boolean playerLucky;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    public Encounter(GameCharacter character, List<Enemy> enemies) {
        if (enemies.isEmpty()) {
            throw new IllegalArgumentException("encounter needs at least one enemy");
        }
        this.character = character;
        this.status = BattleStatus.PLAYER_TURN;
        enemies.forEach(enemy -> this.enemies.add(new EncounterEnemy(enemy)));
        this.startedAt = Instant.now();
    }

    public void updateEnemyHp(int position, int hp) {
        enemies.get(position).setCurrentHp(hp);
    }

    public List<StatusEffect> statusesOf(int combatant) {
        return statuses.stream()
                .filter(status -> status.getCombatant() == combatant)
                .map(EncounterStatus::toEffect)
                .toList();
    }

    public void updateStatuses(int combatant, List<StatusEffect> effects) {
        statuses.removeIf(status -> status.getCombatant() == combatant);
        effects.forEach(effect -> statuses.add(new EncounterStatus(combatant, effect)));
    }

    public void updatePlayerLuck(boolean lucky) {
        this.playerLucky = lucky;
    }

    public void updateStatus(BattleStatus newStatus) {
        this.status = newStatus;
        if (newStatus.isOver()) {
            this.endedAt = Instant.now();
        }
    }
}
