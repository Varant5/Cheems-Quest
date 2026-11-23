package com.cheemsquest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameCharacterRepository extends JpaRepository<GameCharacter, UUID> {
}
