package org.nikolait.assignment.caloriex.service;

import org.nikolait.assignment.caloriex.model.Meal;

import java.time.LocalDate;
import java.util.List;

public interface MealService {

    Meal createMeal(Long userId, Meal meal);

    Meal getUserMeal(Long userId, Long id);

    List<Meal> getAllUserMeals(Long userId);

    List<Meal> getUserMealsForDay(Long userId, LocalDate day);

    List<Meal> getUserMealsBetween(Long userId, LocalDate startDay, LocalDate endDay);

    List<Meal> getUserMealsForToday(Long userId);
}
