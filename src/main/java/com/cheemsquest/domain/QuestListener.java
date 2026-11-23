package com.cheemsquest.domain;

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

@Component
@RequiredArgsConstructor
class QuestListener {

    private final QuestProgressRepository progresses;
    private final GameCharacterRepository characters;

    @EventListener
    @Order(ListenerOrder.QUEST)
    void onEnemyDefeated(EnemyDefeatedEvent event) {
        for (QuestProgress progress : progresses.findByCharacterIdAndStatus(event.characterId(), QuestStatus.ACTIVE)) {
            Quest quest = progress.getQuest();
            if (!quest.targets(event.enemyCode())) {
                continue;
            }
            boolean completed = progress.recordKill();
            event.log().add("Quest progress: " + quest.getName() + " " + progress.getKills() + "/"
                    + quest.getRequiredKills() + ".");
            if (completed) {
                reward(event, quest);
            }
        }
    }

    private void reward(EnemyDefeatedEvent event, Quest quest) {
        GameCharacter character = characters.findById(event.characterId())
                .orElseThrow(() -> new NotFoundException("Character " + event.characterId() + " not found"));

        event.log().add("QUEST COMPLETE! " + quest.getName() + ". Much hero. Very reward.");
        Rewards.grantExperience(character, quest.getRewardExp(), event.log());
        quest.rewardItem().ifPresent(item -> Rewards.grantItem(character, item, event.log()));
    }
}
