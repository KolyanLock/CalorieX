package org.nikolait.assignment.caloriex.dto;

import java.time.Instant;
import java.util.List;

public record MealResponseDto(
        Long id,
        String name,
        List<MealDishResponseDto> mealDishes,
        int calories,
        Instant createdAt
) {
}
