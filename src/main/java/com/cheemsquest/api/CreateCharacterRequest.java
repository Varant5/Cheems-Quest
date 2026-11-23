package com.cheemsquest.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCharacterRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "must be 3-32 letters, digits or underscores")
        String username,

        @NotBlank
        @Size(max = 32)
        String name) {
}
