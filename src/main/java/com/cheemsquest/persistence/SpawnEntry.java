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
public class SpawnEntry {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enemy_code")
    private Enemy enemy;

    @Column(nullable = false)
    private int weight;
}
