package com.cheemsquest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnemyPhase {

    @Column(nullable = false)
    private int phase;

    @Column(name = "hp_threshold", nullable = false)
    private double hpThreshold;

    @Column(nullable = false)
    private String announcement;
}
