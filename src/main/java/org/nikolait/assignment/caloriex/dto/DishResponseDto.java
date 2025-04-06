package org.nikolait.assignment.caloriex.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record DishResponseDto(
        Long id,
        String name,
        Integer calories,
        @Schema(description = "in grams")
        Double protein,
        @Schema(description = "in grams")
        Double fat,
        @Schema(description = "in grams")
        Double carbohydrates,
        Instant createdAt
) {
}
