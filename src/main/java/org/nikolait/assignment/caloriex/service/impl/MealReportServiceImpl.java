package org.nikolait.assignment.caloriex.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.model.Meal;
import org.nikolait.assignment.caloriex.model.MealDailyReport;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.MealReportService;
import org.nikolait.assignment.caloriex.service.MealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealReportServiceImpl implements MealReportService {

    private final MealService mealService;
    private final UserRepository userRepository;

    @Override
    public MealDailyReport generateMealDailyReportForToday(Long userId, ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        return generateMealDailyReportForDay(userId, today, zoneId);
    }

    @Override
    public MealDailyReport generateMealDailyReportForDay(Long userId, LocalDate day, ZoneId zoneId) {
        User user = getUserById(userId);
        List<Meal> dailyMeals = mealService.getUserMealsForDay(userId, day, zoneId);
        int totalCalories = dailyMeals.stream().mapToInt(Meal::getCalories).sum();
        int dailyCalorieTarget = user.getDailyCalorieTarget();
        return new MealDailyReport(day, dailyMeals, dailyCalorieTarget, totalCalories);
    }

    @Override
    public List<MealDailyReport> generateMealDailyReportsForPeriod(
            Long userId,
            LocalDate startDay,
            LocalDate endDay,
            ZoneId zoneId
    ) {
        User user = getUserById(userId);
        int dailyCalorieTarget = user.getDailyCalorieTarget();
        List<Meal> dailyMeals = mealService.getUserMealsBetween(userId, startDay, endDay, zoneId);

        Map<LocalDate, List<Meal>> mealsByDate = dailyMeals.stream()
                .collect(Collectors.groupingBy(meal ->
                        meal.getCreatedAt().atZone(zoneId).toLocalDate()
                ));

        return startDay.datesUntil(endDay.plusDays(1))
                .sorted(Comparator.reverseOrder())
                .map(date -> {
                    List<Meal> meals = mealsByDate.getOrDefault(date, Collections.emptyList());
                    int totalCalories = meals.stream().mapToInt(Meal::getCalories).sum();
                    return new MealDailyReport(date, meals, dailyCalorieTarget, totalCalories);
                })
                .toList();
    }

    @Override
    public List<MealDailyReport> generateAllTrackedMealDailyReports(Long userId, ZoneId zoneId) {
        User user = getUserById(userId);
        int dailyCalorieTarget = user.getDailyCalorieTarget();
        List<Meal> allMeals = mealService.getAllUserMeals(userId);

        Map<LocalDate, List<Meal>> mealsByDate = allMeals.stream()
                .collect(Collectors.groupingBy(meal ->
                        meal.getCreatedAt().atZone(zoneId).toLocalDate()
                ));

        return mealsByDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<Meal>>comparingByKey().reversed())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Meal> meals = entry.getValue();
                    int totalCalories = meals.stream().mapToInt(Meal::getCalories).sum();
                    return new MealDailyReport(date, meals, dailyCalorieTarget, totalCalories);
                })
                .toList();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User with id %d not found".formatted(userId)
                ));
    }
}
