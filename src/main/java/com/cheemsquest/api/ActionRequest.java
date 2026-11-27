package com.cheemsquest.api;

import com.cheemsquest.engine.skill.SkillType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;

public record ActionRequest(
        @Schema(description = "Skill to use. Send either `skill` or `item`.", example = "BONK",
                allowableValues = {"BONK", "BITE", "BONKING_SPIN"})
        SkillType skill,

        @Schema(description = "Code of a consumable from the inventory to use instead of a skill.",
                example = "CHEEMSBURGER")
        String item,

        @Schema(description = "Position of the enemy from the encounter's `enemies` list. "
                + "Optional for BONKING_SPIN, items or when only one enemy is standing.", example = "0")
        @PositiveOrZero
        Integer target) {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "send either a skill or an item")
    public boolean isSingleAction() {
        return (skill == null) != (item == null || item.isBlank());
    }
}
