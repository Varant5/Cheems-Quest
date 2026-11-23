package com.cheemsquest.api;

import com.cheemsquest.engine.skill.SkillType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ActionRequest(
        @Schema(example = "BONK")
        @NotNull
        SkillType skill,

        @Schema(description = "Position of the enemy from the encounter's `enemies` list. "
                + "Optional for BONKING_SPIN or when only one enemy is standing.", example = "0")
        @PositiveOrZero
        Integer target) {
}
