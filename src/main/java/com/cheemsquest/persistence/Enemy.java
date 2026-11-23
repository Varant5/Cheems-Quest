package com.cheemsquest.persistence;

import com.cheemsquest.engine.model.Stats;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

    @ElementCollection
    @CollectionTable(name = "enemy_skill", joinColumns = @JoinColumn(name = "enemy_code"))
    private List<EnemySkill> skills = new ArrayList<>();

    public Stats stats() {
        return new Stats(maxHp, attack, defense);
    }
}
