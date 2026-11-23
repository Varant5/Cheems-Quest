package com.cheemsquest.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCharacterRequest(
        @Schema(description = "Player account name (temporary until login arrives)", example = "bonk_master")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "must be 3-32 letters, digits or underscores")
        String username,

        @Schema(example = "Cheems")
        @NotBlank
        @Size(max = 32)
        String name) {
}
