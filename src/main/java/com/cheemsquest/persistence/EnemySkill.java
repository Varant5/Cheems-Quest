package com.cheemsquest.persistence;

import com.cheemsquest.engine.skill.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnemySkill {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skill;

    @Column(nullable = false)
    private int weight;
}
