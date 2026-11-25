package com.cheemsquest.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TradeRequest(
        @Schema(example = "CHEEMSBURGER") @NotBlank String code,
        @Schema(example = "1") @Min(1) @Max(99) int quantity) {
}
