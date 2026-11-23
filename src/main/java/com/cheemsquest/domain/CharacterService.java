package com.cheemsquest.domain;

import com.cheemsquest.engine.world.Direction;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Item;
import com.cheemsquest.persistence.ItemRepository;
import com.cheemsquest.persistence.Location;
import com.cheemsquest.persistence.Player;
import com.cheemsquest.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final PlayerRepository players;
    private final GameCharacterRepository characters;
    private final ItemRepository items;
    private final EncounterService encounterService;

    // Temporary: the player is identified by username until JWT arrives.
    @Transactional
    public CharacterView create(String username, String characterName) {
        Player player = players.findByUsername(username)
                .orElseGet(() -> players.save(new Player(username)));
        Location home = encounterService.homeLocation();
        Item starterWeapon = items.findById(GameRules.STARTER_WEAPON)
                .orElseThrow(() -> new IllegalStateException("Starter weapon " + GameRules.STARTER_WEAPON + " is missing"));

        GameCharacter character = new GameCharacter(player, characterName, GameRules.STARTING_STATS, home);
        character.equip(character.addItem(starterWeapon));
        return CharacterView.from(characters.save(character));
    }

    @Transactional(readOnly = true)
    public CharacterView get(UUID characterId) {
        return CharacterView.from(load(characterId));
    }

    @Transactional
    public MoveView move(UUID characterId, Direction direction) {
        GameCharacter character = load(characterId);
        encounterService.findActive(characterId).ifPresent(active -> {
            throw new GameRuleException("You can't run from " + EncounterView.from(active).enemyNames() + ". Fight!");
        });

        Location current = character.getLocation();
        Location destination = current.exit(direction)
                .orElseThrow(() -> new GameRuleException(
                        "Much wall. You can't go " + direction + " from " + current.getName() + "."));
        character.moveTo(destination);

        StringBuilder message = new StringBuilder("You enter " + destination.getName() + ".");
        if (destination.isSafe() && character.getCurrentHp() < character.stats().maxHp()) {
            character.restoreHp();
            message.append(" Much nap. HP fully restored.");
        }
        Optional<EncounterView> encounter = encounterService.rollEncounter(character, destination);
        encounter.ifPresent(e -> message.append(' ').append(e.introduction()));

        return new MoveView(message.toString(), LocationView.from(destination),
                character.getCurrentHp(), character.stats().maxHp(), encounter.orElse(null));
    }

    private GameCharacter load(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }
}
