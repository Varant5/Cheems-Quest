package com.cheemsquest.api;

import com.cheemsquest.domain.ActionView;
import com.cheemsquest.domain.EncounterService;
import com.cheemsquest.domain.EncounterView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
class EncounterController {

    private final EncounterService encounterService;

    @GetMapping
    EncounterView current(@PathVariable UUID id) {
        return encounterService.current(id);
    }

    @PostMapping("/action")
    ActionView act(@PathVariable UUID id, @Valid @RequestBody ActionRequest request) {
        return encounterService.act(id, request.skill(), request.target());
    }
}
