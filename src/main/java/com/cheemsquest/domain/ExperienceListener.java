package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ExperienceListener {

    private final GameCharacterRepository characters;

    @EventListener
    void onEnemyDefeated(EnemyDefeatedEvent event) {
        GameCharacter character = characters.findById(event.characterId())
                .orElseThrow(() -> new NotFoundException("Character " + event.characterId() + " not found"));

        int levelsGained = character.gainExperience(event.expReward());

        event.log().add(character.getName() + " gains " + event.expReward() + " EXP.");
        if (levelsGained > 0) {
            event.log().add("LEVEL UP! " + character.getName() + " is now level " + character.getLevel()
                    + ". Much stronger!");
        }
    }
}
