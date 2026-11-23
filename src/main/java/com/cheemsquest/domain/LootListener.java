package com.cheemsquest.domain;

import com.cheemsquest.persistence.Enemy;
import com.cheemsquest.persistence.EnemyRepository;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.random.RandomGenerator;

@Component
@RequiredArgsConstructor
class LootListener {

    private final EnemyRepository enemies;
    private final GameCharacterRepository characters;
    private final RandomGenerator rng;

    @EventListener
    @Order(ListenerOrder.LOOT)
    void onEnemyDefeated(EnemyDefeatedEvent event) {
        Enemy enemy = enemies.findById(event.enemyCode())
                .orElseThrow(() -> new IllegalStateException("Enemy " + event.enemyCode() + " is missing"));

        enemy.lootTable().roll(rng).ifPresent(item -> {
            GameCharacter character = characters.findById(event.characterId())
                    .orElseThrow(() -> new NotFoundException("Character " + event.characterId() + " not found"));
            character.addItem(item);
            event.log().add("Loot! " + event.enemyName() + " drops " + item.getName()
                    + " (" + item.getRarity() + ").");
        });
    }
}
