package com.cheemsquest.engine.random;

import com.cheemsquest.engine.ConstantRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class WeightedTableTest {

    // "common" covers rolls [0, 0.25), "rare" covers [0.25, 1.0)
    private final WeightedTable<String> table = WeightedTable.<String>builder()
            .add("common", 1)
            .add("rare", 3)
            .build();

    @ParameterizedTest
    @CsvSource({
            "0.0,   common",
            "0.249, common",
            "0.25,  rare",
            "0.999, rare"
    })
    void picksEntryWhoseCumulativeRangeContainsTheRoll(double roll, String expected) {
        assertThat(table.pick(new ConstantRandom(roll))).isEqualTo(expected);
    }

    @Test
    void rollAtUpperBoundFallsBackToLastEntry() {
        assertThat(table.pick(new ConstantRandom(Math.nextDown(1.0)))).isEqualTo("rare");
    }

    @Test
    void distributionFollowsWeights() {
        Random seeded = new Random(42);
        int samples = 20_000;

        Map<String, Long> counts = IntStream.range(0, samples)
                .mapToObj(i -> table.pick(seeded))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(counts.get("common") / (double) samples).isCloseTo(0.25, within(0.02));
        assertThat(counts.get("rare") / (double) samples).isCloseTo(0.75, within(0.02));
    }

    @Test
    void uniformGivesEveryValueEqualWeight() {
        WeightedTable<String> uniform = WeightedTable.uniform(List.of("a", "b", "c"));

        assertThat(uniform.totalWeight()).isEqualTo(3);
        assertThat(uniform.entries()).extracting(WeightedTable.Entry::weight).containsOnly(1);
    }

    @Test
    void rejectsEmptyTable() {
        assertThatThrownBy(() -> WeightedTable.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveWeight() {
        assertThatThrownBy(() -> WeightedTable.<String>builder().add("cursed", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
