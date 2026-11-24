package com.cheemsquest.engine.world;

public record GridPosition(int x, int y) {

    public static final GridPosition ORIGIN = new GridPosition(0, 0);
}
