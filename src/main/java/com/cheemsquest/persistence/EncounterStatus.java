package com.cheemsquest.persistence;

import com.cheemsquest.engine.model.StatusEffect;
import com.cheemsquest.engine.model.StatusType;
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
public class EncounterStatus {

    @Column(nullable = false)
    private int combatant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusType type;

    @Column(nullable = false)
    private int turns;

    @Column(nullable = false)
    private int power;

    EncounterStatus(int combatant, StatusEffect effect) {
        this.combatant = combatant;
        this.type = effect.type();
        this.turns = effect.turns();
        this.power = effect.power();
    }

    StatusEffect toEffect() {
        return new StatusEffect(type, turns, power);
    }
}
