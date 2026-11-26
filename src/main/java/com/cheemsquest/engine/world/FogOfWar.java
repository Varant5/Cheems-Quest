package com.cheemsquest.engine.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class FogOfWar {

    private FogOfWar() {
    }

    public static Map<String, Visibility> reveal(Set<String> visited, Map<String, Map<Direction, String>> exits) {
        Map<String, Visibility> revealed = new LinkedHashMap<>();
        for (String code : visited) {
            if (exits.containsKey(code)) {
                revealed.put(code, Visibility.DISCOVERED);
            }
        }
        for (String code : Set.copyOf(revealed.keySet())) {
            for (String neighbor : exits.get(code).values()) {
                revealed.putIfAbsent(neighbor, Visibility.UNKNOWN);
            }
        }
        return revealed;
    }
}
