package com.cheemsquest.domain;

import com.cheemsquest.engine.quest.QuestStatus;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Quest;
import com.cheemsquest.persistence.QuestProgress;
import com.cheemsquest.persistence.QuestProgressRepository;
import com.cheemsquest.persistence.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository quests;
    private final QuestProgressRepository progresses;
    private final GameCharacterRepository characters;

    @Transactional(readOnly = true)
    public List<QuestView> list(UUID characterId) {
        load(characterId);
        Map<String, QuestProgress> progressByQuest = progressByQuest(characterId);

        return quests.findAll(Sort.by("code")).stream()
                .map(quest -> {
                    QuestProgress progress = progressByQuest.get(quest.getCode());
                    if (progress != null) {
                        return QuestView.from(progress);
                    }
                    return isUnlocked(quest, progressByQuest) ? QuestView.available(quest) : QuestView.locked(quest);
                })
                .toList();
    }

    @Transactional
    public QuestView accept(UUID characterId, String questCode) {
        GameCharacter character = load(characterId);
        Quest quest = quests.findById(questCode)
                .orElseThrow(() -> new NotFoundException("Quest " + questCode + " not found"));
        Map<String, QuestProgress> progressByQuest = progressByQuest(characterId);
        if (progressByQuest.containsKey(questCode)) {
            throw new GameRuleException("Quest " + quest.getName() + " was already accepted. Such eager.");
        }
        if (!isUnlocked(quest, progressByQuest)) {
            throw new GameRuleException("Quest " + quest.getName() + " is locked. Finish "
                    + quest.prerequisite().orElseThrow().getName() + " first. Such patience.");
        }

        return QuestView.from(progresses.save(new QuestProgress(character, quest)));
    }

    private Map<String, QuestProgress> progressByQuest(UUID characterId) {
        return progresses.findByCharacterId(characterId).stream()
                .collect(Collectors.toMap(progress -> progress.getQuest().getCode(), Function.identity()));
    }

    private static boolean isUnlocked(Quest quest, Map<String, QuestProgress> progressByQuest) {
        Optional<Quest> required = quest.prerequisite();
        if (required.isEmpty()) {
            return true;
        }
        QuestProgress progress = progressByQuest.get(required.get().getCode());
        return progress != null && progress.getStatus() == QuestStatus.COMPLETED;
    }

    private GameCharacter load(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }
}
