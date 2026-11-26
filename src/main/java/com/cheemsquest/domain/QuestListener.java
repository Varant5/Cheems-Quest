package com.cheemsquest.domain;

import com.cheemsquest.engine.quest.ObjectiveType;
import com.cheemsquest.engine.quest.QuestStatus;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Quest;
import com.cheemsquest.persistence.QuestProgress;
import com.cheemsquest.persistence.QuestProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class QuestListener {

    private final QuestProgressRepository progresses;
    private final GameCharacterRepository characters;
    private final ItemRewards itemRewards;

    @EventListener
    @Order(ListenerOrder.QUEST)
    void onEnemyDefeated(EnemyDefeatedEvent event) {
        advance(event.characterId(), ObjectiveType.DEFEAT_ENEMY, event.enemyCode(), event.log());
    }

    @EventListener
    @Order(ListenerOrder.QUEST)
    void onLocationEntered(LocationEnteredEvent event) {
        advance(event.characterId(), ObjectiveType.VISIT_LOCATION, event.locationCode(), event.log());
    }

    @EventListener
    @Order(ListenerOrder.QUEST)
    void onItemAcquired(ItemAcquiredEvent event) {
        advance(event.characterId(), ObjectiveType.COLLECT_ITEM, event.itemCode(), event.log());
    }

    private void advance(UUID characterId, ObjectiveType type, String targetCode, GameLog log) {
        for (QuestProgress progress : progresses.findByCharacterIdAndStatus(characterId, QuestStatus.ACTIVE)) {
            Quest quest = progress.getQuest();
            if (progress.getStatus() != QuestStatus.ACTIVE || !quest.objective().countsTowards(type, targetCode)) {
                continue;
            }
            boolean completed = progress.advance();
            log.add("Quest progress: " + quest.getName() + " " + progress.getProgress() + "/"
                    + quest.getRequiredAmount() + ".");
            if (completed) {
                reward(characterId, quest, log);
            }
        }
    }

    private void reward(UUID characterId, Quest quest, GameLog log) {
        GameCharacter character = characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));

        log.add("QUEST COMPLETE! " + quest.getName() + ". Much hero. Very reward.");
        Rewards.grantExperience(character, quest.getRewardExp(), log);
        quest.rewardItem().ifPresent(item -> itemRewards.grant(character, item,
                character.getName() + " receives " + item.getName() + " (" + item.getRarity() + ").", log));
    }
}
