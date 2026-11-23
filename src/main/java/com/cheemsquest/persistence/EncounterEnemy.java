package com.cheemsquest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EncounterEnemy {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enemy_code")
    private Enemy enemy;

    @Column(name = "current_hp", nullable = false)
    private int currentHp;

    EncounterEnemy(Enemy enemy) {
        this.enemy = enemy;
        this.currentHp = enemy.getMaxHp();
    }

    void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
}
