package com.cheemsquest.domain;

import com.cheemsquest.engine.world.Direction;
import com.cheemsquest.persistence.Location;

import java.util.List;

public record LocationView(String code, String name, String description, List<Direction> exits) {

    static LocationView from(Location location) {
        List<Direction> exits = location.getExits().keySet().stream().sorted().toList();
        return new LocationView(location.getCode(), location.getName(), location.getDescription(), exits);
    }
}
