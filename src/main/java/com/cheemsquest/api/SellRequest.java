package com.cheemsquest.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SellRequest(
        @Schema(example = "SOGGY_RAINCOAT") @NotBlank String code,
        @Schema(example = "1") @Min(1) int quantity) {
}
