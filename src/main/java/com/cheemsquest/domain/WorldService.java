package com.cheemsquest.domain;

import com.cheemsquest.domain.WorldMapView.MapLocationView;
import com.cheemsquest.engine.world.Direction;
import com.cheemsquest.engine.world.GridPosition;
import com.cheemsquest.engine.world.WorldLayout;
import com.cheemsquest.persistence.Location;
import com.cheemsquest.persistence.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorldService {

    private final LocationRepository locations;

    @Transactional(readOnly = true)
    public WorldMapView map() {
        Map<String, Location> byCode = locations.findAll().stream()
                .collect(Collectors.toMap(Location::getCode, Function.identity()));
        Map<String, Map<Direction, String>> exits = byCode.values().stream()
                .collect(Collectors.toMap(Location::getCode, WorldService::exitCodes));
        Map<String, GridPosition> layout = WorldLayout.layout(GameRules.HOME_LOCATION, exits);

        List<MapLocationView> placed = layout.entrySet().stream()
                .map(entry -> {
                    Location location = byCode.get(entry.getKey());
                    GridPosition position = entry.getValue();
                    return new MapLocationView(location.getCode(), location.getName(),
                            position.x(), position.y(), location.isSafe(), exits.get(location.getCode()),
                            gateRequirements(location));
                })
                .sorted(Comparator.comparingInt(MapLocationView::y).thenComparingInt(MapLocationView::x))
                .toList();
        int width = placed.stream().mapToInt(MapLocationView::x).max().orElse(0) + 1;
        int height = placed.stream().mapToInt(MapLocationView::y).max().orElse(0) + 1;
        return new WorldMapView(width, height, placed);
    }

    private static Map<Direction, List<String>> gateRequirements(Location location) {
        Map<Direction, List<String>> gates = new EnumMap<>(Direction.class);
        location.getGates().forEach((direction, gate) -> gates.put(direction, gate.requirements()));
        return gates;
    }

    private static Map<Direction, String> exitCodes(Location location) {
        Map<Direction, String> codes = new EnumMap<>(Direction.class);
        location.getExits().forEach((direction, target) -> codes.put(direction, target.getCode()));
        return codes;
    }
}
