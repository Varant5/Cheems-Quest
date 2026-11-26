package com.cheemsquest.domain;

import com.cheemsquest.engine.quest.ObjectiveType;
import com.cheemsquest.engine.quest.QuestStatus;
import com.cheemsquest.persistence.Item;
import com.cheemsquest.persistence.Quest;
import com.cheemsquest.persistence.QuestProgress;

public record QuestView(
        String code,
        String name,
        String description,
        ObjectiveType objectiveType,
        String target,
        int requiredAmount,
        int progress,
        QuestStatus status,
        int rewardExp,
        String rewardItem) {

    static QuestView available(Quest quest) {
        return of(quest, 0, QuestStatus.AVAILABLE);
    }

    static QuestView from(QuestProgress progress) {
        return of(progress.getQuest(), progress.getProgress(), progress.getStatus());
    }

    private static QuestView of(Quest quest, int progress, QuestStatus status) {
        return new QuestView(quest.getCode(), quest.getName(), quest.getDescription(),
                quest.getObjectiveType(), quest.targetName(), quest.getRequiredAmount(), progress, status,
                quest.getRewardExp(), quest.rewardItem().map(Item::getName).orElse(null));
    }
}
