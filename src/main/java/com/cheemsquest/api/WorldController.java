package com.cheemsquest.api;

import com.cheemsquest.domain.WorldMapView;
import com.cheemsquest.domain.WorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/characters/{id}/map")
@RequiredArgsConstructor
@PreAuthorize(SecurityConfig.OWNS_CHARACTER)
@Tag(name = "World", description = "Much map. Very explore.")
class WorldController {

    private final WorldService worldService;

    @GetMapping
    @Operation(summary = "Get the character's map",
            description = "Locations the character has visited, plus their unexplored neighbours named \"???\". "
                    + "x grows to the east, y grows to the south, (0, 0) is the top-left corner of the explored area. "
                    + "Positions are derived from the exits, starting at Cheems' Doghouse.")
    @ApiResponse(responseCode = "200", description = "Explored part of the world")
    @ApiResponse(responseCode = "403", description = "The character belongs to another player",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    WorldMapView map(@PathVariable UUID id) {
        return worldService.map(id);
    }
}
