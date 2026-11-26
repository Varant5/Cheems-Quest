package com.cheemsquest.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExitGate {

    @Column(name = "required_level")
    private Integer requiredLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_quest_code")
    private Quest requiredQuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_item_code")
    private Item requiredItem;

    public List<String> requirements() {
        return unmetBy(0, Set.of(), Set.of());
    }

    public List<String> unmetBy(int level, Set<String> completedQuestCodes, Set<String> itemCodes) {
        List<String> unmet = new ArrayList<>();
        if (requiredLevel != null && level < requiredLevel) {
            unmet.add("level " + requiredLevel);
        }
        if (requiredQuest != null && !completedQuestCodes.contains(requiredQuest.getCode())) {
            unmet.add("quest " + requiredQuest.getName());
        }
        if (requiredItem != null && !itemCodes.contains(requiredItem.getCode())) {
            unmet.add(requiredItem.getName());
        }
        return unmet;
    }
}
