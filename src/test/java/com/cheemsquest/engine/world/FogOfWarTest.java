package com.cheemsquest.engine.world;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class FogOfWarTest {

    private final Map<String, Map<Direction, String>> exits = Map.of(
            "HOME", Map.of(Direction.NORTH, "FOREST", Direction.EAST, "BASEMENT"),
            "FOREST", Map.of(Direction.SOUTH, "HOME", Direction.NORTH, "TOWER"),
            "BASEMENT", Map.of(Direction.WEST, "HOME"),
            "TOWER", Map.of(Direction.SOUTH, "FOREST"));

    @Test
    void visitedLocationsAreDiscoveredAndTheirNeighboursAreUnknown() {
        assertThat(FogOfWar.reveal(Set.of("HOME"), exits)).containsOnly(
                entry("HOME", Visibility.DISCOVERED),
                entry("FOREST", Visibility.UNKNOWN),
                entry("BASEMENT", Visibility.UNKNOWN));
    }

    @Test
    void neighbourThatWasVisitedStaysDiscovered() {
        assertThat(FogOfWar.reveal(Set.of("HOME", "FOREST"), exits)).containsOnly(
                entry("HOME", Visibility.DISCOVERED),
                entry("FOREST", Visibility.DISCOVERED),
                entry("BASEMENT", Visibility.UNKNOWN),
                entry("TOWER", Visibility.UNKNOWN));
    }

    @Test
    void ignoresVisitedCodesMissingFromTheWorld() {
        assertThat(FogOfWar.reveal(Set.of("ATLANTIS"), exits)).isEmpty();
    }
}
