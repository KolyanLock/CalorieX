package org.nikolait.assignment.caloriex.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.nikolait.assignment.caloriex.model.GenderEnum;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Integer age,
        @Schema(description = "kilograms")
        Double weight,
        @Schema(description = "centimeters")
        Integer height,
        GenderEnum gender,
        ActivityLevelResponseDto activityLevel,
        GoalResponseDto goal,
        Integer dailyCalorieTarget,
        Instant createdAt
) {
}
