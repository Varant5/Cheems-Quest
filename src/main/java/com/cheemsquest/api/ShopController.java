package com.cheemsquest.api;

import com.cheemsquest.domain.InventoryView;
import com.cheemsquest.domain.ShopService;
import com.cheemsquest.domain.ShopView;
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
@RequestMapping("/api/characters/{id}/shop")
@RequiredArgsConstructor
@PreAuthorize(SecurityConfig.OWNS_CHARACTER)
@ApiResponse(responseCode = "403", description = "The character belongs to another player",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@Tag(name = "Shop", description = "Spend gold where the character stands. Much shopping.")
class ShopController {

    private final ShopService shopService;

    @GetMapping
    @Operation(summary = "Browse the shop", description = "Offers of the shop in the character's current location.")
    @ApiResponse(responseCode = "200", description = "Shop offers and current gold")
    @ApiResponse(responseCode = "404", description = "No such character",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No shop here or the character is in a fight",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    ShopView get(@PathVariable UUID id) {
        return shopService.get(id);
    }

    @PostMapping("/buy")
    @Operation(summary = "Buy items", description = "Pays gold and puts the items into the inventory.")
    @ApiResponse(responseCode = "200", description = "Bought, inventory with updated gold")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No such character or the shop does not sell the item",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No shop here, in a fight or not enough gold",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    InventoryView buy(@PathVariable UUID id, @Valid @RequestBody TradeRequest request) {
        return shopService.buy(id, request.code(), request.quantity());
    }
}
