package com.cheemsquest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameCharacterRepository extends JpaRepository<GameCharacter, UUID> {

    List<GameCharacter> findByPlayerIdOrderByCreatedAt(UUID playerId);

    @Query("select c.player.id from GameCharacter c where c.id = :characterId")
    Optional<UUID> findOwnerId(UUID characterId);
}
