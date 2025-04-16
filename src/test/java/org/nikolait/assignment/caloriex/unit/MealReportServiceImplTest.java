package org.nikolait.assignment.caloriex.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.nikolait.assignment.caloriex.UnitTestBase;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.MealService;
import org.nikolait.assignment.caloriex.service.impl.MealReportServiceImpl;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class MealReportServiceImplTest extends UnitTestBase {

    // Test constants
    private static final Long USER_ID = 1L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Paris");
    private static final int DAILY_TARGET = 2000;

    @Mock
    private MealService mealService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealReportServiceImpl mealReportService;

    private User testUser;
    private Meal meal1;
    private Meal meal2;
    private Meal meal3;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .dailyCalorieTarget(DAILY_TARGET)
                .build();

        // Create test dishes
        Dish dish500 = createDish(500);
        Dish dish300 = createDish(300);
        Dish dish600 = createDish(600);

        // Create meals with meal dishes
        meal1 = createMeal("2024-01-10T09:00:00Z", List.of(
                Pair.of(2.0, dish500),  // 2 * 500 = 1000
                Pair.of(1.0, dish300)   // 1 * 300 = 300
        )); // Total: 1300

        meal2 = createMeal("2024-01-15T10:00:00Z", List.of(
                Pair.of(1.5, dish600)  // 1.5 * 600 = 900
        ));

        meal3 = createMeal("2024-01-15T15:30:00Z", List.of(
                Pair.of(3.0, dish300) // 3 * 300 = 900
        ));
    }

    private Dish createDish(int calories) {
        return Dish.builder()
                .id(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)
                .calories(calories)
                .build();
    }

    private Meal createMeal(String timestamp, List<Pair<Double, Dish>> dishes) {
        List<MealDish> mealDishes = dishes.stream()
                .map(pair -> {
                    MealDish md = new MealDish();
                    md.setServings(pair.getFirst());
                    md.setDish(pair.getSecond());
                    return md;
                })
                .collect(Collectors.toList());

        Meal meal = Meal.builder()
                .createdAt(Instant.parse(timestamp))
                .mealDishes(mealDishes)
                .build();

        mealDishes.forEach(md -> md.setMeal(meal));
        return meal;
    }

    @Nested
    @DisplayName("Daily Report Tests")
    class DailyReportTests {

        @Test
        @DisplayName("Generate report for current day")
        void generateDailyReportForToday() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsForDay(eq(USER_ID), any(LocalDate.class), eq(TEST_ZONE)))
                    .thenReturn(List.of(meal2, meal3));

            MealDailyReport report = mealReportService.generateMealDailyReportForToday(USER_ID, TEST_ZONE);

            assertAll(
                    () -> assertEquals(LocalDate.now(TEST_ZONE), report.getDate()),
                    () -> assertEquals(900 + 900, report.getTotalCalories()),
                    () -> assertEquals(DAILY_TARGET, report.getDailyCalorieTarget()),
                    () -> assertEquals(2, report.getMeals().size()),
                    () -> assertFalse(report.isExceeded())
            );
        }

        @Test
        @DisplayName("Generate report for specific date")
        void generateDailyReportForSpecificDay() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsForDay(USER_ID, TEST_DATE, TEST_ZONE))
                    .thenReturn(List.of(meal2, meal3));

            MealDailyReport report = mealReportService.generateMealDailyReportForDay(
                    USER_ID, TEST_DATE, TEST_ZONE
            );

            assertAll(
                    () -> assertEquals(TEST_DATE, report.getDate()),
                    () -> assertEquals(900 + 900, report.getTotalCalories()),
                    () -> assertEquals(2, report.getMeals().size()),
                    () -> assertEquals(DAILY_TARGET, report.getDailyCalorieTarget())
            );
        }

        @Test
        @DisplayName("Detect calorie limit exceed")
        void calorieExceededCheck() {
            Meal highCalorieMeal = createMeal("2024-01-16T12:00:00Z", List.of(
                    Pair.of(4.0, createDish(600)) // 4 * 600 = 2400
            ));

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsForDay(USER_ID, LocalDate.now(TEST_ZONE), TEST_ZONE))
                    .thenReturn(List.of(highCalorieMeal));

            MealDailyReport report = mealReportService.generateMealDailyReportForToday(USER_ID, TEST_ZONE);

            assertAll(
                    () -> assertEquals(2400, report.getTotalCalories()),
                    () -> assertTrue(report.isExceeded())
            );
        }

        @Test
        @DisplayName("Handle missing user scenario")
        void dailyReportUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> mealReportService.generateMealDailyReportForDay(
                            USER_ID, TEST_DATE, TEST_ZONE
                    ));
        }
    }

    @Nested
    @DisplayName("Period Report Tests")
    class PeriodReportTests {

        @Test
        @DisplayName("Generate multi-day reports")
        void generateReportsForPeriod() {
            LocalDate start = LocalDate.of(2024, 1, 10);
            LocalDate end = LocalDate.of(2024, 1, 15);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsBetween(USER_ID, start, end, TEST_ZONE))
                    .thenReturn(List.of(meal1, meal2, meal3));

            List<MealDailyReport> reports = mealReportService
                    .generateMealDailyReportsForPeriod(USER_ID, start, end, TEST_ZONE);

            assertAll(
                    () -> assertEquals(6, reports.size()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), reports.getFirst().getDate()),
                    () -> assertEquals(900 + 900, reports.getFirst().getTotalCalories()),
                    () -> assertEquals(LocalDate.of(2024, 1, 10), reports.get(5).getDate()),
                    () -> assertEquals(1300, reports.get(5).getTotalCalories())
            );
        }

        @Test
        @DisplayName("Handle period with no meals")
        void generateReportsWithEmptyDays() {
            LocalDate start = TEST_DATE;
            LocalDate end = TEST_DATE.plusDays(3);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsBetween(USER_ID, start, end, TEST_ZONE))
                    .thenReturn(Collections.emptyList());

            List<MealDailyReport> reports = mealReportService
                    .generateMealDailyReportsForPeriod(USER_ID, start, end, TEST_ZONE);

            assertAll(
                    () -> assertEquals(4, reports.size()),
                    () -> assertTrue(reports.stream().allMatch(r -> r.getTotalCalories() == 0))
            );
        }
    }

    @Nested
    @DisplayName("Comprehensive Report Tests")
    class AllReportsTests {

        @Test
        @DisplayName("Generate all tracked meal reports")
        void generateAllTrackedReportsSorted() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getAllUserMeals(USER_ID))
                    .thenReturn(List.of(meal1, meal2, meal3));

            List<MealDailyReport> reports = mealReportService
                    .generateAllTrackedMealDailyReports(USER_ID, TEST_ZONE);

            assertAll(
                    () -> assertEquals(2, reports.size()),
                    () -> assertEquals(LocalDate.of(2024, 1, 15), reports.getFirst().getDate()),
                    () -> assertEquals(LocalDate.of(2024, 1, 10), reports.get(1).getDate()),
                    () -> assertEquals(900 + 900, reports.getFirst().getTotalCalories()),
                    () -> assertEquals(1300, reports.get(1).getTotalCalories())
            );
        }

        @Test
        @DisplayName("Handle empty meal history")
        void generateAllReportsWithNoMeals() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getAllUserMeals(USER_ID))
                    .thenReturn(Collections.emptyList());

            List<MealDailyReport> reports = mealReportService
                    .generateAllTrackedMealDailyReports(USER_ID, TEST_ZONE);

            assertTrue(reports.isEmpty());
        }
    }

    @Nested
    @DisplayName("Special Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Handle daylight saving time transition")
        void handleDaylightSavingTime() {
            ZoneId dstZone = ZoneId.of("America/New_York");
            LocalDate testDate = LocalDate.of(2023, 3, 12);

            Meal meal = createMeal("2023-03-12T07:00:00Z", List.of(
                    Pair.of(1.0, createDish(500)) // Post DST transition
            ));

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsForDay(USER_ID, testDate, dstZone))
                    .thenReturn(List.of(meal));

            MealDailyReport report = mealReportService.generateMealDailyReportForDay(
                    USER_ID, testDate, dstZone);

            assertEquals(500, report.getTotalCalories());
        }

        @Test
        @DisplayName("Validate timezone conversion logic")
        void timeZoneConversionTest() {
            ZoneId pacificZone = ZoneId.of("Pacific/Auckland");
            LocalDate targetDate = LocalDate.of(2024, 1, 16);

            Meal meal = createMeal("2024-01-15T11:30:00Z", List.of(
                    Pair.of(1.0, createDish(700)) // UTC time converts to next day in NZ
            ));

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(mealService.getUserMealsForDay(
                    USER_ID,
                    targetDate,
                    pacificZone
            )).thenReturn(List.of(meal));

            MealDailyReport report = mealReportService.generateMealDailyReportForDay(
                    USER_ID,
                    targetDate,
                    pacificZone
            );

            assertAll(
                    () -> assertEquals(targetDate, report.getDate()),
                    () -> assertEquals(700, report.getTotalCalories())
            );
        }
    }

}
