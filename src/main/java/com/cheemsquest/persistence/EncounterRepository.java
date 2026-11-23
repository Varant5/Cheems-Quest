package com.cheemsquest.persistence;

import com.cheemsquest.engine.combat.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EncounterRepository extends JpaRepository<Encounter, UUID> {

    Optional<Encounter> findByCharacterIdAndStatus(UUID characterId, BattleStatus status);
}
