package com.cheemsquest.domain;

import com.cheemsquest.domain.WorldMapView.MapLocationView;
import com.cheemsquest.engine.world.Direction;
import com.cheemsquest.engine.world.FogOfWar;
import com.cheemsquest.engine.world.GridPosition;
import com.cheemsquest.engine.world.Visibility;
import com.cheemsquest.engine.world.WorldLayout;
import com.cheemsquest.persistence.GameCharacter;
import com.cheemsquest.persistence.GameCharacterRepository;
import com.cheemsquest.persistence.Location;
import com.cheemsquest.persistence.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorldService {

    private static final String UNKNOWN_NAME = "???";

    private final LocationRepository locations;
    private final GameCharacterRepository characters;

    @Transactional(readOnly = true)
    public WorldMapView map(UUID characterId) {
        GameCharacter character = characters.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Character " + characterId + " not found"));
        Map<String, Location> byCode = locations.findAll().stream()
                .collect(Collectors.toMap(Location::getCode, Function.identity()));
        Map<String, Map<Direction, String>> exits = byCode.values().stream()
                .collect(Collectors.toMap(Location::getCode, WorldService::exitCodes));
        Map<String, GridPosition> layout = WorldLayout.layout(GameRules.HOME_LOCATION, exits);
        Map<String, Visibility> revealed = FogOfWar.reveal(character.visitedLocationCodes(), exits);

        int minX = revealed.keySet().stream().mapToInt(code -> layout.get(code).x()).min().orElse(0);
        int minY = revealed.keySet().stream().mapToInt(code -> layout.get(code).y()).min().orElse(0);
        List<MapLocationView> shown = revealed.entrySet().stream()
                .map(entry -> {
                    Location location = byCode.get(entry.getKey());
                    GridPosition position = layout.get(entry.getKey());
                    int x = position.x() - minX;
                    int y = position.y() - minY;
                    return entry.getValue() == Visibility.DISCOVERED
                            ? new MapLocationView(location.getCode(), location.getName(), x, y, true,
                                    location.isSafe(), exits.get(location.getCode()), gateRequirements(location))
                            : new MapLocationView(location.getCode(), UNKNOWN_NAME, x, y, false,
                                    false, Map.of(), Map.of());
                })
                .sorted(Comparator.comparingInt(MapLocationView::y).thenComparingInt(MapLocationView::x))
                .toList();
        int width = shown.stream().mapToInt(MapLocationView::x).max().orElse(0) + 1;
        int height = shown.stream().mapToInt(MapLocationView::y).max().orElse(0) + 1;
        return new WorldMapView(width, height, shown);
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
