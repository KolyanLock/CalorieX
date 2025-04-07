package org.nikolait.assignment.caloriex.dto;

import java.time.LocalDate;
import java.util.List;

public record MealDailyReportDto(
        LocalDate date,
        List<MealResponseDto> meals,
        int totalCalories,
        int dailyCalorieTarget,
        boolean isExceeded
) {
}
