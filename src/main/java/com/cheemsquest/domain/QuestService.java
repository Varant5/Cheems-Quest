package com.cheemsquest.domain;

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
        Map<String, QuestProgress> progressByQuest = progresses.findByCharacterId(characterId).stream()
                .collect(Collectors.toMap(progress -> progress.getQuest().getCode(), Function.identity()));

        return quests.findAll(Sort.by("code")).stream()
                .map(quest -> {
                    QuestProgress progress = progressByQuest.get(quest.getCode());
                    return progress == null ? QuestView.available(quest) : QuestView.from(progress);
                })
                .toList();
    }

    @Transactional
    public QuestView accept(UUID characterId, String questCode) {
        GameCharacter character = load(characterId);
        Quest quest = quests.findById(questCode)
                .orElseThrow(() -> new NotFoundException("Quest " + questCode + " not found"));
        boolean alreadyTaken = progresses.findByCharacterId(characterId).stream()
                .anyMatch(progress -> progress.getQuest().getCode().equals(questCode));
        if (alreadyTaken) {
            throw new GameRuleException("Quest " + quest.getName() + " was already accepted. Such eager.");
        }

        return QuestView.from(progresses.save(new QuestProgress(character, quest)));
    }

    private GameCharacter load(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }
}
