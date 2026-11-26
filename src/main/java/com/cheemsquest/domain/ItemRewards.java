package com.cheemsquest.domain;

import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ItemRewards {

    private final ApplicationEventPublisher events;

    void grant(GameCharacter character, Item item, String message, GameLog log) {
        character.addItem(item);
        log.add(message);
        events.publishEvent(new ItemAcquiredEvent(character.getId(), item.getCode(), log));
    }
}
