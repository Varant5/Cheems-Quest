package com.cheemsquest.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(example = "Cheems")
        @NotBlank
        @Size(max = 32)
        String username,

        @Schema(example = "Cheems-safe")
        @NotBlank
        @Size(max = 72)
        String password) {
}
