package com.cheemsquest.engine.world;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WorldLayout {

    private WorldLayout() {
    }

    public static Map<String, GridPosition> layout(String start, Map<String, Map<Direction, String>> exits) {
        if (!exits.containsKey(start)) {
            throw new IllegalArgumentException("Unknown start location " + start);
        }
        Map<String, GridPosition> positions = new LinkedHashMap<>();
        Map<GridPosition, String> occupied = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        place(start, GridPosition.ORIGIN, positions, occupied);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            GridPosition from = positions.get(current);
            for (Map.Entry<Direction, String> exit : exits.getOrDefault(current, Map.of()).entrySet()) {
                String target = exit.getValue();
                GridPosition expected = exit.getKey().step(from);
                GridPosition known = positions.get(target);
                if (known == null) {
                    place(target, expected, positions, occupied);
                    queue.add(target);
                } else if (!known.equals(expected)) {
                    throw new IllegalArgumentException("Exit " + exit.getKey() + " from " + current + " leads to "
                            + target + ", which does not fit on a grid");
                }
            }
        }
        return normalize(positions);
    }

    private static void place(String code, GridPosition position,
                              Map<String, GridPosition> positions, Map<GridPosition, String> occupied) {
        String other = occupied.putIfAbsent(position, code);
        if (other != null) {
            throw new IllegalArgumentException(code + " and " + other + " overlap at " + position);
        }
        positions.put(code, position);
    }

    private static Map<String, GridPosition> normalize(Map<String, GridPosition> positions) {
        int minX = positions.values().stream().mapToInt(GridPosition::x).min().orElse(0);
        int minY = positions.values().stream().mapToInt(GridPosition::y).min().orElse(0);
        Map<String, GridPosition> normalized = new LinkedHashMap<>();
        positions.forEach((code, p) -> normalized.put(code, new GridPosition(p.x() - minX, p.y() - minY)));
        return normalized;
    }
}
