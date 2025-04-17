package org.nikolait.assignment.caloriex.integration;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nikolait.assignment.caloriex.IntegrationTestBase;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.service.MealService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MealServiceTest extends IntegrationTestBase {

    // Constants for reference data and defaults
    private static final Long SEDENTARY_ACTIVITY_ID = 1L;
    private static final Long WEIGHT_LOSS_GOAL_ID = 1L;
    private static final int TEST_AGE = 30;
    private static final double TEST_WEIGHT = 80.0;
    private static final int TEST_HEIGHT = 170;
    private static final ZoneId TEST_ZONE = ZoneId.of("America/New_York");
    private static final double DEFAULT_SERVINGS = 1.0;

    @Autowired
    private MealService mealService;

    // Test fixtures
    private ActivityLevel sedentaryActivityLevel;
    private Goal weightLossGoal;
    private User testUser;
    private User anotherUser;
    private Dish userDish;
    private Dish anotherUserDish;

    @BeforeEach
    void setUp() {
        // Load reference data from migrations
        sedentaryActivityLevel = activityLevelRepository.findById(SEDENTARY_ACTIVITY_ID)
                .orElseThrow(() -> new IllegalStateException("SEDENTARY activity level not found"));
        weightLossGoal = goalRepository.findById(WEIGHT_LOSS_GOAL_ID)
                .orElseThrow(() -> new IllegalStateException("WEIGHT_LOSS goal not found"));

        // Create test users
        testUser = createTestUser("Test User", "test@example.com");
        anotherUser = createTestUser("Another User", "another@example.com");

        // Create dishes for each user
        userDish = createDish(testUser, "User's Dish", 300);
        anotherUserDish = createDish(anotherUser, "Another's Dish", 500);
    }

    @Test
    @DisplayName("Creating meal with valid dishes persists meal with correct relationships and calories")
    void createMeal_WithValidDishes_PersistsWithCorrectRelationsAndCalculations() {
        // Arrange: create an additional dish for the test user
        Dish secondDish = createDish(testUser, "Second Dish", 200);

        // Build a meal payload with two dishes and varying servings
        Meal meal = Meal.builder()
                .name("Test Meal")
                .mealDishes(new ArrayList<>())
                .build();
        meal.getMealDishes().add(buildMealDish(userDish, 2.0));
        meal.getMealDishes().add(buildMealDish(secondDish, 1.5));

        // Act: create meal through service
        Meal createdMeal = mealService.createMeal(testUser.getId(), meal);

        // Assert: check persistence and calculations
        assertNotNull(createdMeal.getId());
        assertEquals(testUser.getId(), createdMeal.getUser().getId());
        assertEquals(2, createdMeal.getMealDishes().size());
        assertEquals(900, createdMeal.getCalories());

        // Verify data stored in the repository
        Meal persistedMeal = mealRepository.findById(createdMeal.getId())
                .orElseThrow();
        assertEquals(2, persistedMeal.getMealDishes().size());
    }

    @Test
    @DisplayName("Creating meal with non-existent dish throws UnprocessableEntityException")
    void createMeal_WithNonExistentDish_ThrowsException() {
        // Arrange: build meal with a dish ID that does not exist
        Dish fakeDish = Dish.builder().id(999L).build();
        Meal meal = Meal.builder()
                .mealDishes(List.of(buildMealDish(fakeDish, DEFAULT_SERVINGS)))
                .build();

        // Act & Assert: expect exception for invalid dish
        assertThrows(UnprocessableEntityException.class,
                () -> mealService.createMeal(testUser.getId(), meal));
    }

    @Test
    @DisplayName("Retrieving user meal returns only meals belonging to specified user")
    void getUserMeal_ReturnsOnlyOwnersMeals() {
        // Arrange: create meals for two different users
        Meal usersMeal = createMealThroughService(testUser, List.of(userDish), "User Meal");
        Meal othersMeal = createMealThroughService(anotherUser, List.of(anotherUserDish), "Another Meal");

        // Act: retrieve only the test user's meal
        Meal retrieved = mealService.getUserMeal(testUser.getId(), usersMeal.getId());

        // Assert: correct meal returned and unauthorized access blocked
        assertEquals(usersMeal.getId(), retrieved.getId());
        assertThrows(EntityNotFoundException.class,
                () -> mealService.getUserMeal(testUser.getId(), othersMeal.getId()));
    }

    @Test
    @DisplayName("getAllUserMeals returns all meals belonging to user")
    void getAllUserMeals_ReturnsAllMealsForUser() {
        // Arrange: create multiple meals for testUser and anotherUser
        Meal meal1 = createMealThroughService(testUser, List.of(userDish), "Meal One");
        Meal meal2 = createMealThroughService(testUser, List.of(userDish), "Meal Two");
        createMealThroughService(anotherUser, List.of(anotherUserDish), "Other Meal");

        // Act
        List<Meal> allMeals = mealService.getAllUserMeals(testUser.getId());

        // Assert
        assertThat(allMeals)
                .extracting(Meal::getId)
                .containsExactlyInAnyOrder(meal1.getId(), meal2.getId());
    }

    @Test
    @DisplayName("getUserMealsForDay returns meals only for the specified day")
    void getUserMealsForDay_ReturnsMealsForGivenDay() {
        // Arrange: fixed date and timezone
        LocalDate today = LocalDate.now(TEST_ZONE);
        Instant startOfDay = today.atStartOfDay(TEST_ZONE).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(TEST_ZONE).minusSeconds(1).toInstant();
        Instant outOfRange = today.plusDays(1).atStartOfDay(TEST_ZONE).plusSeconds(1).toInstant();

        // Create meals before, within, and after the day
        Meal beforeMeal = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), startOfDay.minusSeconds(1));
        Meal validMeal1 = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), startOfDay);
        Meal validMeal2 = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), endOfDay);
        Meal afterMeal = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), outOfRange);

        // Act
        List<Meal> dayMeals = mealService.getUserMealsForDay(
                testUser.getId(), today, TEST_ZONE);

        // Assert
        assertThat(dayMeals)
                .extracting(Meal::getId)
                .containsExactlyInAnyOrder(validMeal1.getId(), validMeal2.getId())
                .doesNotContain(beforeMeal.getId(), afterMeal.getId());
    }

    @Test
    @DisplayName("getUserMealsBetween returns only meals for a single day, timezone-aware")
    void getUserMealsBetween_RespectsTimezoneBoundaries() {
        // Arrange: use fixed date to avoid timezone discrepancies
        LocalDate referenceDate = LocalDate.of(2025, 4, 16);
        Instant startOfDay = referenceDate.atStartOfDay(TEST_ZONE).toInstant();
        Instant justBeforeEnd = referenceDate.plusDays(1).atStartOfDay(TEST_ZONE).minusSeconds(1).toInstant();
        Instant justAfterEnd = referenceDate.plusDays(1).atStartOfDay(TEST_ZONE).plusSeconds(1).toInstant();

        // Create meals around the day boundaries
        Meal mealBefore = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), startOfDay.minusSeconds(1));
        Meal mealAtStart = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), startOfDay);
        Meal mealBeforeEnd = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), justBeforeEnd);
        Meal mealAfter = createMealWithSpecificTime(testUser, List.of(buildMealDish(userDish, DEFAULT_SERVINGS)), justAfterEnd);

        // Act: retrieve meals only for the reference date
        List<Meal> filtered = mealService.getUserMealsBetween(
                testUser.getId(), referenceDate, referenceDate, TEST_ZONE);

        // Assert: only meals within the day are returned
        assertThat(filtered)
                .extracting(Meal::getId)
                .containsExactlyInAnyOrder(mealAtStart.getId(), mealBeforeEnd.getId())
                .doesNotContain(mealBefore.getId(), mealAfter.getId());
    }

    // Helper methods

    /**
     * Creates and persists a test user with default profile settings.
     */
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

    /**
     * Creates and persists a dish for the given user.
     */
    private Dish createDish(User owner, String name, int calories) {
        return dishRepository.save(Dish.builder()
                .name(name)
                .user(owner)
                .calories(calories)
                .build());
    }

    /**
     * Builds a MealDish linking the given dish and servings.
     */
    private MealDish buildMealDish(Dish dish, double servings) {
        MealDish mealDish = new MealDish();
        mealDish.setId(new MealDishId());
        mealDish.setDish(dish);
        mealDish.setServings(servings);
        return mealDish;
    }

    /**
     * Creates a meal through the service, assigning dishes and default settings.
     */
    private Meal createMealThroughService(User owner, List<Dish> dishes, String name) {
        Meal meal = Meal.builder().name(name).mealDishes(new ArrayList<>()).build();
        dishes.forEach(d -> meal.getMealDishes().add(buildMealDish(d, DEFAULT_SERVINGS)));
        return mealService.createMeal(owner.getId(), meal);
    }

    /**
     * Creates and persists a meal at a specific instant with given dishes.
     */
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
