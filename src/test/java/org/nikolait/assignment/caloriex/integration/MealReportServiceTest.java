package org.nikolait.assignment.caloriex.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assignment.caloriex.IntegrationTestBase;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.service.MealReportService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MealReportServiceTest extends IntegrationTestBase {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDate DATE_1 = LocalDate.now(TEST_ZONE).minusDays(7);
    private static final LocalDate DATE_2 = LocalDate.now(TEST_ZONE).minusDays(2);
    private static final int TEST_AGE = 30;
    private static final double TEST_WEIGHT = 80.0;
    private static final int TEST_HEIGHT = 170;

    @Autowired
    private MealReportService mealReportService;

    private ActivityLevel sedentaryActivityLevel;
    private Goal weightLossGoal;

    private User testUser;
    private User anotherUser;
    private Dish pastaDish;
    private Dish saladDish;

    @BeforeEach
    void setUp() {
        sedentaryActivityLevel = activityLevelRepository.findById(1L).orElseThrow();
        weightLossGoal = goalRepository.findById(1L).orElseThrow();

        testUser = createTestUser("Alice", "alice@example.com");
        anotherUser = createTestUser("Bob", "bob@example.com");

        pastaDish = createDish(testUser, "Pasta Bolognese", 500);
        saladDish = createDish(testUser, "Green Salad", 300);

        createMealWithSpecificTime(testUser,
                List.of(
                        buildMealDish(pastaDish, 2.0),
                        buildMealDish(saladDish, 1.0)
                ),
                DATE_1.atStartOfDay(TEST_ZONE).plusHours(9).toInstant()
        );

        createMealWithSpecificTime(testUser,
                List.of(buildMealDish(saladDish, 3.0)),
                DATE_2.atStartOfDay(TEST_ZONE).plusHours(10).toInstant()
        );
    }

    @Test
    @DisplayName("Ensure report is generated only for meals of the specified user")
    void generateReportForSpecificDay() {
        createMealWithSpecificTime(anotherUser,
                List.of(buildMealDish(createDish(anotherUser, "OtherUserDish", 999), 1.0)),
                DATE_1.atStartOfDay(TEST_ZONE).plusHours(11).toInstant()
        );

        var report = mealReportService.generateMealDailyReportForDay(testUser.getId(), DATE_1, TEST_ZONE);

        assertAll(
                () -> assertEquals(DATE_1, report.getDate()),
                () -> assertEquals(1300, report.getTotalCalories()),
                () -> assertEquals(testUser.getDailyCalorieTarget(), report.getDailyCalorieTarget()),
                () -> assertFalse(report.isExceeded()),
                () -> assertThat(report.getMeals()).allMatch(m -> m.getUser().getId().equals(testUser.getId()))
        );
    }

    @Test
    @DisplayName("Generate period report with meals and gaps")
    void generatePeriodReportWithGaps() {
        var reports = mealReportService.generateMealDailyReportsForPeriod(testUser.getId(), DATE_1, DATE_2, TEST_ZONE);

        assertEquals(DATE_2.toEpochDay() - DATE_1.toEpochDay() + 1, reports.size());

        var reportStart = reports.stream().filter(r -> r.getDate().equals(DATE_1)).findFirst().orElseThrow();
        var reportEnd = reports.stream().filter(r -> r.getDate().equals(DATE_2)).findFirst().orElseThrow();

        assertThat(reportStart.getTotalCalories()).isEqualTo(1300);
        assertThat(reportEnd.getTotalCalories()).isEqualTo(900);
    }

    @Test
    @DisplayName("Generate all tracked reports sorted descending")
    void generateAllTrackedReportsSorted() {
        var reports = mealReportService.generateAllTrackedMealDailyReports(testUser.getId(), TEST_ZONE);

        assertEquals(2, reports.size());
        assertTrue(reports.get(0).getDate().isAfter(reports.get(1).getDate()));
    }

    @Test
    @DisplayName("Generate reports for period with no meals")
    void generateReportsForEmptyPeriod() {
        LocalDate start = LocalDate.now(TEST_ZONE).minusMonths(3);
        LocalDate end = start.plusDays(4);

        var reports = mealReportService.generateMealDailyReportsForPeriod(testUser.getId(), start, end, TEST_ZONE);

        assertEquals(5, reports.size());
        assertTrue(reports.stream().allMatch(r -> r.getTotalCalories() == 0));
    }

    @Test
    @DisplayName("Generate all tracked reports with no meals")
    void generateAllTrackedReportsWhenEmpty() {
        mealRepository.deleteAll();

        var reports = mealReportService.generateAllTrackedMealDailyReports(testUser.getId(), TEST_ZONE);

        assertTrue(reports.isEmpty());
    }

    private User createTestUser(String name, String email) {
        User user = User.builder()
                .name(name)
                .email(email)
                .age(TEST_AGE)
                .weight(TEST_WEIGHT)
                .height(TEST_HEIGHT)
                .gender(GenderEnum.MALE)
                .activityLevel(sedentaryActivityLevel)
                .goal(weightLossGoal)
                .build();
        user.setDailyCalorieTarget(CalorieCalculator.calculateDailyCalorieTarget(user));
        return userRepository.save(user);
    }

    private Dish createDish(User owner, String name, int calories) {
        return dishRepository.save(Dish.builder()
                .name(name)
                .user(owner)
                .calories(calories)
                .build());
    }

    private MealDish buildMealDish(Dish dish, double servings) {
        MealDish md = new MealDish();
        md.setId(new MealDishId());
        md.setDish(dish);
        md.setServings(servings);
        return md;
    }

    private Meal createMealWithSpecificTime(User owner, List<MealDish> mealDishes, Instant createdAt) {
        Meal meal = Meal.builder()
                .user(owner)
                .createdAt(createdAt)
                .build();
        mealRepository.save(meal);

        mealDishes.forEach(md -> md.setMeal(meal));
        meal.setMealDishes(mealDishes);

        return mealRepository.save(meal);
    }

}
