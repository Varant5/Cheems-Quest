package com.cheemsquest.persistence;

import com.cheemsquest.engine.quest.QuestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quest_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id")
    private GameCharacter character;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_code")
    private Quest quest;

    @Column(nullable = false)
    private int kills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private QuestStatus status;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public QuestProgress(GameCharacter character, Quest quest) {
        this.character = character;
        this.quest = quest;
        this.kills = 0;
        this.status = QuestStatus.ACTIVE;
        this.acceptedAt = Instant.now();
    }

    public boolean recordKill() {
        if (status != QuestStatus.ACTIVE) {
            throw new IllegalStateException("quest " + quest.getCode() + " is not active");
        }
        kills++;
        if (kills >= quest.getRequiredKills()) {
            status = QuestStatus.COMPLETED;
            completedAt = Instant.now();
            return true;
        }
        return false;
    }
}
