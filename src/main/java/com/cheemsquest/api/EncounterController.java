package com.cheemsquest.api;

import com.cheemsquest.domain.ActionView;
import com.cheemsquest.domain.EncounterService;
import com.cheemsquest.domain.EncounterView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/characters/{id}/encounter")
@RequiredArgsConstructor
@PreAuthorize(SecurityConfig.OWNS_CHARACTER)
@ApiResponse(responseCode = "403", description = "The character belongs to another player",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@Tag(name = "Encounters", description = "Turn-based fights. Such bonk.")
class EncounterController {

    private final EncounterService encounterService;

    @GetMapping
    @Operation(summary = "Get the ongoing fight", description = "Enemies with their positions and HP.")
    @ApiResponse(responseCode = "200", description = "Fight in progress")
    @ApiResponse(responseCode = "404", description = "No such character or no ongoing fight",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    EncounterView current(@PathVariable UUID id) {
        return encounterService.current(id);
    }

    @PostMapping("/action")
    @Operation(summary = "Use a skill or an item",
            description = "Plays one round: your skill or consumable, then every living enemy counterattacks. "
                    + "The `log` describes what happened. On defeat the character wakes up at home with full HP.")
    @ApiResponse(responseCode = "200", description = "Round played")
    @ApiResponse(responseCode = "400", description = "Unknown skill, invalid target, or not exactly one of skill and item",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No such character or the item is not in the inventory",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No ongoing fight, target missing or already down, item not usable now, "
            + "or another action was played at the same time",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    ActionView act(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        if (request.item() != null && !request.item().isBlank()) {
            return encounterService.useItem(id, request.item());
        }
        return encounterService.act(id, request.skill(), request.target());
    }
}
