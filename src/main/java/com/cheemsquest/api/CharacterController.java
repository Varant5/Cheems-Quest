package com.cheemsquest.api;

import com.cheemsquest.domain.CharacterService;
import com.cheemsquest.domain.CharacterView;
import com.cheemsquest.domain.MoveView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
class CharacterController {

    private final CharacterService characterService;

    @PostMapping
    ResponseEntity<CharacterView> create(@Valid @RequestBody CreateCharacterRequest request) {
        CharacterView character = characterService.create(request.username(), request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(character.id())
                .toUri();
        return ResponseEntity.created(location).body(character);
    }

    @GetMapping("/{id}")
    CharacterView get(@PathVariable UUID id) {
        return characterService.get(id);
    }

    @PostMapping("/{id}/move")
    MoveView move(@PathVariable UUID id, @Valid @RequestBody MoveRequest request) {
        return characterService.move(id, request.direction());
    }
}
