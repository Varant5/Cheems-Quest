package com.cheemsquest.engine.progression;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProgressionTest {

    @ParameterizedTest
    @CsvSource({"1, 0", "2, 50", "3, 150", "4, 300", "5, 500"})
    void thresholdGrowsWithEachLevel(int level, int experience) {
        assertThat(Progression.experienceForLevel(level)).isEqualTo(experience);
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "49, 1", "50, 2", "149, 2", "150, 3", "1000, 6", "1050, 7"})
    void levelForExperience(int experience, int level) {
        assertThat(Progression.levelFor(experience)).isEqualTo(level);
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> Progression.experienceForLevel(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Progression.levelFor(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
