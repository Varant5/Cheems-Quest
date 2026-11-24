package com.cheemsquest.api;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenView(
        @Schema(description = "Paste it into the Authorize dialog or send it as the Authorization: Bearer header")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Seconds until the token expires", example = "3600")
        long expiresIn,

        @Schema(example = "bonk_master")
        String username) {
}
