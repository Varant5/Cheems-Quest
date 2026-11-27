package com.cheemsquest.domain;

import com.cheemsquest.engine.combat.Battle;
import com.cheemsquest.engine.combat.BattleStatus;
import com.cheemsquest.engine.combat.EnemyAi;
import com.cheemsquest.engine.combat.RoundResult;
import com.cheemsquest.engine.combat.WeightedEnemyAi;
import com.cheemsquest.engine.item.ItemEffect;
import com.cheemsquest.engine.model.Combatant;
import com.cheemsquest.engine.model.StatusType;
import com.cheemsquest.engine.narration.BattleNarrator;
import com.cheemsquest.engine.random.WeightedTable;
import com.cheemsquest.engine.skill.Skill;
import com.cheemsquest.engine.skill.SkillType;
import com.cheemsquest.persistence.Encounter;
import com.cheemsquest.persistence.EncounterEnemy;
import com.cheemsquest.persistence.EncounterRepository;
import com.cheemsquest.persistence.Enemy;
import com.cheemsquest.persistence.EnemySkill;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.InventorySlot;
import com.cheemsquest.persistence.Item;
import com.cheemsquest.persistence.Location;
import com.cheemsquest.persistence.LocationRepository;
import com.cheemsquest.persistence.SpawnEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EncounterService {

    private final EncounterRepository encounters;
    private final GameCharacterRepository characters;
    private final LocationRepository locations;
    private final RandomGenerator rng;
    private final ApplicationEventPublisher events;

    @Transactional(readOnly = true)
    public EncounterView current(UUID characterId) {
        return findActive(characterId)
                .map(EncounterView::from)
                .orElseThrow(() -> new NotFoundException("No active encounter for character " + characterId));
    }

    @Transactional
    public ActionView act(UUID characterId, SkillType skillType, Integer targetPosition) {
        GameCharacter character = loadCharacter(characterId);
        Encounter encounter = loadActive(characterId);
        if (!skillType.isPlayerSkill()) {
            throw new GameRuleException(character.getName() + " doesn't know " + skillType.skill().name()
                    + ". Much confuse.");
        }
        return playRound(character, encounter, skillType.skill(), targetPosition);
    }

    @Transactional
    public ActionView useItem(UUID characterId, String itemCode) {
        GameCharacter character = loadCharacter(characterId);
        Encounter encounter = loadActive(characterId);
        InventorySlot slot = character.unequippedCopies(itemCode).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No " + itemCode + " in " + character.getName() + "'s inventory"));
        Item item = slot.getItem();
        if (!item.isConsumable()) {
            throw new GameRuleException(item.getName() + " is not something you can use. Try a skill instead.");
        }
        Skill itemSkill = item.getEffect().inBattle(item.getName(), item.getEffectPower())
                .orElseThrow(() -> new GameRuleException(item.getName() + " needs a calm moment. Use it outside of a fight."));
        if (item.getEffect() == ItemEffect.HEAL && character.getCurrentHp() >= character.stats().maxHp()) {
            throw new GameRuleException("Already at full HP. Save the " + item.getName() + " for later.");
        }
        if (item.getEffect() == ItemEffect.GUARANTEED_CRIT && encounter.isPlayerLucky()) {
            throw new GameRuleException("Already feeling lucky. Save the " + item.getName() + " for later.");
        }
        boolean stunned = encounter.statusesOf(Encounter.PLAYER).stream()
                .anyMatch(effect -> effect.type() == StatusType.STUN);
        if (!stunned) {
            character.removeItem(slot);
        }
        return playRound(character, encounter, itemSkill, null);
    }

    private ActionView playRound(GameCharacter character, Encounter encounter, Skill skill, Integer targetPosition) {
        UUID characterId = character.getId();
        Combatant player = new Combatant(character.getName(), character.stats(), character.getCurrentHp());
        if (encounter.isPlayerLucky()) {
            player.bless();
        }
        encounter.statusesOf(Encounter.PLAYER).forEach(player::afflict);
        List<Combatant> foes = IntStream.range(0, encounter.getEnemies().size())
                .mapToObj(i -> {
                    Combatant foe = toCombatant(encounter.getEnemies().get(i));
                    encounter.statusesOf(i).forEach(foe::afflict);
                    return foe;
                })
                .toList();
        List<Integer> hpBefore = encounter.getEnemies().stream().map(EncounterEnemy::getCurrentHp).toList();
        Battle battle = new Battle(player, foes, aiFor(encounter, foes), rng);

        RoundResult round = runRound(battle, skill, foes, targetPosition);

        encounter.updatePlayerLuck(player.isLucky());
        encounter.updateStatuses(Encounter.PLAYER, player.getStatuses());
        character.updateHp(player.getHp());
        for (int i = 0; i < foes.size(); i++) {
            encounter.updateEnemyHp(i, foes.get(i).getHp());
            encounter.updateStatuses(i, foes.get(i).getStatuses());
        }
        encounter.updateStatus(round.status());

        List<String> log = new ArrayList<>(BattleNarrator.narrate(round));
        switch (round.status()) {
            case VICTORY -> log.add("VICTORY! " + character.getName() + " earns much respect.");
            case DEFEAT -> {
                Location home = homeLocation();
                character.respawnAt(home);
                log.add(character.getName() + " faints and wakes up in " + home.getName()
                        + " with full HP. Much shame.");
            }
            case PLAYER_TURN, ENEMY_TURN -> {
            }
        }

        GameLog rewards = new GameLog();
        for (int i = 0; i < foes.size(); i++) {
            if (hpBefore.get(i) > 0 && !foes.get(i).isAlive()) {
                Enemy defeated = encounter.getEnemies().get(i).getEnemy();
                events.publishEvent(new EnemyDefeatedEvent(
                        characterId, defeated.getCode(), defeated.getName(), defeated.getExpReward(), rewards));
            }
        }
        log.addAll(rewards.lines());

        return new ActionView(log, round.status(), character.getCurrentHp(), character.stats().maxHp(),
                EncounterView.from(encounter));
    }

    private GameCharacter loadCharacter(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }

    private Encounter loadActive(UUID characterId) {
        return findActive(characterId)
                .orElseThrow(() -> new GameRuleException("Nothing to bonk here. Such peace."));
    }

    Optional<Encounter> findActive(UUID characterId) {
        return encounters.findByCharacterIdAndStatus(characterId, BattleStatus.PLAYER_TURN);
    }

    Optional<EncounterView> rollEncounter(GameCharacter character, Location location) {
        if (location.isSafe() || rng.nextDouble() >= location.getEncounterChance()) {
            return Optional.empty();
        }
        WeightedTable.Builder<Enemy> spawnTable = WeightedTable.builder();
        for (SpawnEntry spawn : location.getSpawns()) {
            spawnTable.add(spawn.getEnemy(), spawn.getWeight());
        }
        Enemy enemy = spawnTable.build().pick(rng);

        Encounter encounter = encounters.save(new Encounter(character, List.of(enemy)));
        return Optional.of(EncounterView.from(encounter));
    }

    Location homeLocation() {
        return locations.findById(GameRules.HOME_LOCATION)
                .orElseThrow(() -> new IllegalStateException("Home location " + GameRules.HOME_LOCATION + " is missing"));
    }

    private static RoundResult runRound(Battle battle, Skill skill, List<Combatant> foes, Integer targetPosition) {
        try {
            if (targetPosition == null) {
                return battle.act(skill);
            }
            if (targetPosition < 0 || targetPosition >= foes.size()) {
                throw new GameRuleException("There is no enemy at position " + targetPosition + ".");
            }
            return battle.act(skill, foes.get(targetPosition));
        } catch (IllegalArgumentException e) {
            throw new GameRuleException(e.getMessage());
        }
    }

    private static Combatant toCombatant(EncounterEnemy encounterEnemy) {
        Enemy enemy = encounterEnemy.getEnemy();
        return new Combatant(enemy.getName(), enemy.stats(), encounterEnemy.getCurrentHp());
    }

    // Battle takes a single EnemyAi, so dispatch to each enemy's own skill table.
    private static EnemyAi aiFor(Encounter encounter, List<Combatant> foes) {
        Map<Combatant, EnemyAi> ais = new IdentityHashMap<>();
        for (int i = 0; i < foes.size(); i++) {
            ais.put(foes.get(i), skillAi(encounter.getEnemies().get(i).getEnemy()));
        }
        return (enemy, random) -> ais.get(enemy).chooseSkill(enemy, random);
    }

    private static EnemyAi skillAi(Enemy enemy) {
        WeightedTable.Builder<Skill> skills = WeightedTable.builder();
        for (EnemySkill enemySkill : enemy.getSkills()) {
            skills.add(enemySkill.getSkill().skill(), enemySkill.getWeight());
        }
        return new WeightedEnemyAi(skills.build());
    }
}
