package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ExperienceListener {

    private final GameCharacterRepository characters;

    @EventListener
    @Order(ListenerOrder.EXPERIENCE)
    void onEnemyDefeated(EnemyDefeatedEvent event) {
        GameCharacter character = characters.findById(event.characterId())
                .orElseThrow(() -> new NotFoundException("Character " + event.characterId() + " not found"));

        Rewards.grantExperience(character, event.expReward(), event.log());
    }
}
