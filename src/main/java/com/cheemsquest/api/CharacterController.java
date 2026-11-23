package com.cheemsquest.api;

import com.cheemsquest.domain.CharacterService;
import com.cheemsquest.domain.CharacterView;
import com.cheemsquest.domain.MoveView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Tag(name = "Characters", description = "Create Cheems and walk around the world")
class CharacterController {

    private final CharacterService characterService;

    @PostMapping
    @Operation(summary = "Create a character",
            description = "Creates the player if the username is new. The character starts in Cheems' Doghouse.")
    @ApiResponse(responseCode = "201", description = "Character created")
    @ApiResponse(responseCode = "400", description = "Invalid username or name",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<CharacterView> create(@Valid @RequestBody CreateCharacterRequest request) {
        CharacterView character = characterService.create(request.username(), request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(character.id())
                .toUri();
        return ResponseEntity.created(location).body(character);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a character", description = "Stats, HP and the current location with its exits.")
    @ApiResponse(responseCode = "200", description = "Character found")
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    CharacterView get(@PathVariable UUID id) {
        return characterService.get(id);
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "Move in a direction",
            description = "Entering a dangerous location may start a fight: check `encounter` in the response. "
                    + "Entering a safe location restores HP.")
    @ApiResponse(responseCode = "200", description = "Moved")
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No exit that way, or the character is in a fight",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    MoveView move(@PathVariable UUID id, @Valid @RequestBody MoveRequest request) {
        return characterService.move(id, request.direction());
    }
}
