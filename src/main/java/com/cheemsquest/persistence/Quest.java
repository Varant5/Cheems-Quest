package com.cheemsquest.persistence;

import com.cheemsquest.engine.quest.ObjectiveType;
import com.cheemsquest.engine.quest.QuestObjective;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Optional;

@Entity
@Table(name = "quest")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quest {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "objective_type", nullable = false, length = 32)
    private ObjectiveType objectiveType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_enemy_code")
    private Enemy targetEnemy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_code")
    private Location targetLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_item_code")
    private Item targetItem;

    @Column(name = "required_amount", nullable = false)
    private int requiredAmount;

    @Column(name = "reward_exp", nullable = false)
    private int rewardExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_item_code")
    private Item rewardItem;

    public Optional<Item> rewardItem() {
        return Optional.ofNullable(rewardItem);
    }

    public QuestObjective objective() {
        String targetCode = switch (objectiveType) {
            case DEFEAT_ENEMY -> targetEnemy.getCode();
            case VISIT_LOCATION -> targetLocation.getCode();
            case COLLECT_ITEM -> targetItem.getCode();
        };
        return new QuestObjective(objectiveType, targetCode, requiredAmount);
    }

    public String targetName() {
        return switch (objectiveType) {
            case DEFEAT_ENEMY -> targetEnemy.getName();
            case VISIT_LOCATION -> targetLocation.getName();
            case COLLECT_ITEM -> targetItem.getName();
        };
    }
}
