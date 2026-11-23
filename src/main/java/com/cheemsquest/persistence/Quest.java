package com.cheemsquest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Optional;

@Entity
@Table(name = "quest")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quest {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_enemy_code")
    private Enemy targetEnemy;

    @Column(name = "required_kills", nullable = false)
    private int requiredKills;

    @Column(name = "reward_exp", nullable = false)
    private int rewardExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_item_code")
    private Item rewardItem;

    public Optional<Item> rewardItem() {
        return Optional.ofNullable(rewardItem);
    }

    public boolean targets(String enemyCode) {
        return targetEnemy.getCode().equals(enemyCode);
    }
}
