package com.cheemsquest.api;

import com.cheemsquest.domain.WorldMapView;
import com.cheemsquest.domain.WorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/world")
@RequiredArgsConstructor
@Tag(name = "World", description = "Much map. Very explore.")
class WorldController {

    private final WorldService worldService;

    @GetMapping("/map")
    @Operation(summary = "Get the world map",
            description = "Every location on a grid: x grows to the east, y grows to the south, (0, 0) is the top-left "
                    + "corner. Positions are derived from the exits, starting at Cheems' Doghouse.")
    @ApiResponse(responseCode = "200", description = "World map")
    WorldMapView map() {
        return worldService.map();
    }
}
