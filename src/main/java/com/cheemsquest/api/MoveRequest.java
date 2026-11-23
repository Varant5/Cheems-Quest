package com.cheemsquest.api;

import com.cheemsquest.engine.world.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(@Schema(example = "NORTH") @NotNull Direction direction) {
}
