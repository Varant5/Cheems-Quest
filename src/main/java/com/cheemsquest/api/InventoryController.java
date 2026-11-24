package com.cheemsquest.api;

import com.cheemsquest.domain.InventoryService;
import com.cheemsquest.domain.InventoryView;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/characters/{id}/inventory")
@RequiredArgsConstructor
@PreAuthorize(SecurityConfig.OWNS_CHARACTER)
@ApiResponse(responseCode = "403", description = "The character belongs to another player",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@Tag(name = "Inventory", description = "Items and equipment. Much loot, very drip.")
class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "List the inventory", description = "All items with their bonuses, plus stats including equipment.")
    @ApiResponse(responseCode = "200", description = "Inventory")
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    InventoryView get(@PathVariable UUID id) {
        return inventoryService.get(id);
    }

    @PostMapping("/{itemId}/equip")
    @Operation(summary = "Equip an item", description = "Replaces whatever was equipped in the same slot.")
    @ApiResponse(responseCode = "200", description = "Equipped")
    @ApiResponse(responseCode = "404", description = "No such character or item",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "The character is in a fight",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    InventoryView equip(@PathVariable UUID id, @PathVariable UUID itemId) {
        return inventoryService.equip(id, itemId);
    }

    @PostMapping("/{itemId}/unequip")
    @Operation(summary = "Unequip an item", description = "Current HP is capped if max HP drops.")
    @ApiResponse(responseCode = "200", description = "Unequipped")
    @ApiResponse(responseCode = "404", description = "No such character or item",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "The character is in a fight",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    InventoryView unequip(@PathVariable UUID id, @PathVariable UUID itemId) {
        return inventoryService.unequip(id, itemId);
    }
}
