package com.cheemsquest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestRepository extends JpaRepository<Quest, String> {
}
