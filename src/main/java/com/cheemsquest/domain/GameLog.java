package com.cheemsquest.domain;

import java.util.ArrayList;
import java.util.List;

public final class GameLog {

    private final List<String> lines = new ArrayList<>();

    public void add(String line) {
        lines.add(line);
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }
}
