package com.cheemsquest.domain;

import com.cheemsquest.engine.world.Direction;

import java.util.List;
import java.util.Map;

public record WorldMapView(int width, int height, List<MapLocationView> locations) {

    public record MapLocationView(String code, String name, int x, int y, boolean safe, Map<Direction, String> exits) {
    }
}
