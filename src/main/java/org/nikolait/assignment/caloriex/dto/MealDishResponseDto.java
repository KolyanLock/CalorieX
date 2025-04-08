package org.nikolait.assignment.caloriex.dto;

public record MealDishResponseDto(
        DishResponseDto dish,
        Double servings,
        Double calories
) {
}
