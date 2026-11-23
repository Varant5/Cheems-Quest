package com.cheemsquest.api;

import com.cheemsquest.engine.world.Direction;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(@NotNull Direction direction) {
}
