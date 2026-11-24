package com.cheemsquest.engine.world;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.cheemsquest.engine.world.Direction.EAST;
import static com.cheemsquest.engine.world.Direction.NORTH;
import static com.cheemsquest.engine.world.Direction.SOUTH;
import static com.cheemsquest.engine.world.Direction.WEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class WorldLayoutTest {

    private static final Map<String, Map<Direction, String>> CHEEMS_WORLD = Map.of(
            "HOME", Map.of(NORTH, "FOREST", EAST, "BASEMENT"),
            "BASEMENT", Map.of(WEST, "HOME"),
            "FOREST", Map.of(SOUTH, "HOME", WEST, "SWAMP", NORTH, "TOWER"),
            "SWAMP", Map.of(EAST, "FOREST"),
            "TOWER", Map.of(SOUTH, "FOREST"));

    @Test
    void placesLocationsOnAGridStartingAtTopLeft() {
        Map<String, GridPosition> layout = WorldLayout.layout("HOME", CHEEMS_WORLD);

        assertThat(layout).containsOnly(
                entry("TOWER", new GridPosition(1, 0)),
                entry("SWAMP", new GridPosition(0, 1)),
                entry("FOREST", new GridPosition(1, 1)),
                entry("HOME", new GridPosition(1, 2)),
                entry("BASEMENT", new GridPosition(2, 2)));
    }

    @Test
    void singleLocationSitsAtOrigin() {
        assertThat(WorldLayout.layout("HOME", Map.of("HOME", Map.of())))
                .containsOnly(entry("HOME", GridPosition.ORIGIN));
    }

    @Test
    void skipsLocationsThatCannotBeReached() {
        Map<String, GridPosition> layout = WorldLayout.layout("HOME", Map.of(
                "HOME", Map.of(EAST, "SHOP"),
                "SHOP", Map.of(WEST, "HOME"),
                "SECRET", Map.of()));

        assertThat(layout).containsOnlyKeys("HOME", "SHOP");
    }

    @Test
    void rejectsTwoLocationsOnTheSameCell() {
        assertThatThrownBy(() -> WorldLayout.layout("HOME", Map.of(
                "HOME", Map.of(NORTH, "A", EAST, "B"),
                "A", Map.of(EAST, "C"),
                "B", Map.of(NORTH, "D"),
                "C", Map.of(),
                "D", Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlap");
    }

    @Test
    void rejectsExitsThatBendTheGrid() {
        assertThatThrownBy(() -> WorldLayout.layout("HOME", Map.of(
                "HOME", Map.of(NORTH, "FOREST", EAST, "FOREST"),
                "FOREST", Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not fit on a grid");
    }

    @Test
    void unknownStartIsRejected() {
        assertThatThrownBy(() -> WorldLayout.layout("NOWHERE", CHEEMS_WORLD))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
