package com.hamplz.quizjam.quizroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
    @NotNull Long quizId,
    @Min(5) @Max(300) Integer questionTimeLimitSeconds
) {
}
