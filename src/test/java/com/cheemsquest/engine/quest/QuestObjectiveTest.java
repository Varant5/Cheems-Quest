package com.cheemsquest.engine.quest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestObjectiveTest {

    private final QuestObjective karens = new QuestObjective(ObjectiveType.DEFEAT_ENEMY, "KAREN_DOGE", 3);

    @Test
    void countsOnlyMatchingTypeAndTarget() {
        assertThat(karens.countsTowards(ObjectiveType.DEFEAT_ENEMY, "KAREN_DOGE")).isTrue();
        assertThat(karens.countsTowards(ObjectiveType.DEFEAT_ENEMY, "LIL_DOGE")).isFalse();
    }

    @Test
    void isMetOnceProgressReachesTheRequiredAmount() {
        assertThat(karens.isMet(2)).isFalse();
        assertThat(karens.isMet(3)).isTrue();
        assertThat(karens.isMet(4)).isTrue();
    }

    @Test
    void rejectsInvalidObjectives() {
        assertThatThrownBy(() -> new QuestObjective(ObjectiveType.DEFEAT_ENEMY, "KAREN_DOGE", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new QuestObjective(null, "KAREN_DOGE", 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new QuestObjective(ObjectiveType.DEFEAT_ENEMY, null, 1))
                .isInstanceOf(NullPointerException.class);
    }
}
