package com.cheemsquest.domain;

import com.cheemsquest.engine.model.Stats;
import com.cheemsquest.engine.world.Direction;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Location;
import com.cheemsquest.persistence.LocationRepository;
import com.cheemsquest.persistence.Player;
import com.cheemsquest.persistence.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CharacterService {

    static final Stats STARTING_STATS = new Stats(65, 14, 3);
    static final String START_LOCATION = "CHEEMS_DOGHOUSE";

    private final PlayerRepository players;
    private final GameCharacterRepository characters;
    private final LocationRepository locations;

    // Temporary: the player is identified by username until JWT arrives in Phase 3.
    @Transactional
    public CharacterView create(String username, String characterName) {
        Player player = players.findByUsername(username)
                .orElseGet(() -> players.save(new Player(username)));
        Location start = locations.findById(START_LOCATION)
                .orElseThrow(() -> new IllegalStateException("Start location " + START_LOCATION + " is missing"));

        GameCharacter character = characters.save(new GameCharacter(player, characterName, STARTING_STATS, start));
        return CharacterView.from(character);
    }

    @Transactional(readOnly = true)
    public CharacterView get(UUID characterId) {
        return CharacterView.from(load(characterId));
    }

    @Transactional
    public MoveView move(UUID characterId, Direction direction) {
        GameCharacter character = load(characterId);
        Location current = character.getLocation();
        Location destination = current.exit(direction)
                .orElseThrow(() -> new GameRuleException(
                        "Much wall. You can't go " + direction + " from " + current.getName() + "."));

        character.moveTo(destination);
        return new MoveView("You enter " + destination.getName() + ".", LocationView.from(destination));
    }

    private GameCharacter load(UUID characterId) {
        return characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
    }
}
