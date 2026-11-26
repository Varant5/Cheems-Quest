package com.cheemsquest.engine.quest;

import java.util.Objects;

public record QuestObjective(ObjectiveType type, String targetCode, int requiredAmount) {

    public QuestObjective {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(targetCode, "targetCode");
        if (requiredAmount <= 0) {
            throw new IllegalArgumentException("required amount must be positive, got " + requiredAmount);
        }
    }

    public boolean countsTowards(ObjectiveType eventType, String eventTargetCode) {
        return type == eventType && targetCode.equals(eventTargetCode);
    }

    public boolean isMet(int progress) {
        return progress >= requiredAmount;
    }
}
