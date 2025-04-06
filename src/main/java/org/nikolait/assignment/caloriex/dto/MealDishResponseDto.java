package org.nikolait.assignment.caloriex.dto;

public record MealDishResponseDto(
        Long dishId,
        String dishName,
        Double servings,
        Double calories
) {
}
