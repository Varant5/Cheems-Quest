package com.cheemsquest.persistence;

import com.cheemsquest.engine.quest.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestProgressRepository extends JpaRepository<QuestProgress, UUID> {

    List<QuestProgress> findByCharacterId(UUID characterId);

    List<QuestProgress> findByCharacterIdAndStatus(UUID characterId, QuestStatus status);
}
