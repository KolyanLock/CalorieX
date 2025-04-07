package org.nikolait.assignment.caloriex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealDailyReport {

    private LocalDate date;

    private List<Meal> meals;

    private int dailyCalorieTarget;

    private int totalCalories;

    public boolean isExceeded() {
        return totalCalories > dailyCalorieTarget;
    }

}
