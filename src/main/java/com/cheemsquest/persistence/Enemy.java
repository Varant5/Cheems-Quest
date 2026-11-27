package com.cheemsquest.persistence;

import com.cheemsquest.engine.combat.EnemyAi;
import com.cheemsquest.engine.combat.PhasedEnemyAi;
import com.cheemsquest.engine.combat.TacticalEnemyAi;
import com.cheemsquest.engine.loot.GoldDrop;
import com.cheemsquest.engine.loot.LootTable;
import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Skill;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enemy")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Enemy {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private int attack;

    @Column(nullable = false)
    private int defense;

    @Column(name = "exp_reward", nullable = false)
    private int expReward;

    @Column(name = "loot_chance", nullable = false)
    private double lootChance;

    @Column(name = "gold_min", nullable = false)
    private int goldMin;

    @Column(name = "gold_max", nullable = false)
    private int goldMax;

    @ElementCollection
    @CollectionTable(name = "enemy_skill", joinColumns = @JoinColumn(name = "enemy_code"))
    @OrderBy("weight DESC")
    private List<EnemySkill> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "enemy_phase", joinColumns = @JoinColumn(name = "enemy_code"))
    @OrderBy("phase")
    private List<EnemyPhase> phases = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "enemy_loot", joinColumns = @JoinColumn(name = "enemy_code"))
    @OrderBy("weight DESC")
    private List<LootEntry> loot = new ArrayList<>();

    public GoldDrop goldDrop() {
        return new GoldDrop(goldMin, goldMax);
    }

    public Stats stats() {
        return new Stats(maxHp, attack, defense);
    }

    public EnemyAi ai() {
        EnemyAi opening = phaseAi(1);
        if (phases.isEmpty()) {
            return opening;
        }
        List<PhasedEnemyAi.Phase> laterPhases = phases.stream()
                .map(phase -> new PhasedEnemyAi.Phase(phase.getHpThreshold(), phase.getAnnouncement(),
                        phaseAi(phase.getPhase())))
                .toList();
        return new PhasedEnemyAi(opening, laterPhases);
    }

    private EnemyAi phaseAi(int phase) {
        WeightedTable.Builder<Skill> table = WeightedTable.builder();
        skills.stream()
                .filter(skill -> skill.getPhase() == phase)
                .forEach(skill -> table.add(skill.getSkill().skill(), skill.getWeight()));
        return new TacticalEnemyAi(table.build());
    }

    public LootTable<Item> lootTable() {
        if (loot.isEmpty()) {
            return LootTable.empty();
        }
        WeightedTable.Builder<Item> drops = WeightedTable.builder();
        loot.forEach(entry -> drops.add(entry.getItem(), entry.getWeight()));
        return LootTable.of(lootChance, drops.build());
    }
}
