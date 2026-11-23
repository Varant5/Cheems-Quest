package com.cheemsquest.api;

import com.cheemsquest.engine.skill.SkillType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ActionRequest(@NotNull SkillType skill, @PositiveOrZero Integer target) {
}
