package org.nikolait.assignment.caloriex.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikolait.assignment.caloriex.IntegrationTestBase;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.repository.ActivityLevelRepository;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.GoalRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.DishService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DishServiceTest extends IntegrationTestBase {

    // Constants for reference data from migrations
    private static final Long SEDENTARY_ACTIVITY_ID = 1L;
    private static final Long WEIGHT_LOSS_GOAL_ID = 1L;

    // Constants for test data
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_USER_EMAIL = "test.user@example.com";
    private static final String OTHER_USER_EMAIL = "other@example.com";
    private static final String DISH_1_NAME = "Chicken Salad";
    private static final String DISH_2_NAME = "Protein Shake";
    private static final String PASTA_NAME = "Pasta";

    // Macronutrients for test dishes
    private static final Double DISH_1_PROTEIN = 30.0;
    private static final Double DISH_1_FAT = 10.0;
    private static final Double DISH_1_CARBS = 5.0;

    private static final Double PASTA_PROTEIN = 25.0;
    private static final Double PASTA_FAT = 10.0;
    private static final Double PASTA_CARBS = 50.0;

    @Autowired
    private DishService dishService;

    private ActivityLevel sedentaryActivityLevel;
    private Goal weighLossGoal;

    private User testUser;

    @BeforeEach
    void setUp() {
        sedentaryActivityLevel = activityLevelRepository.findById(SEDENTARY_ACTIVITY_ID)
                .orElseThrow(() -> new IllegalStateException("SEDENTARY activity level not found"));
        weighLossGoal = goalRepository.findById(WEIGHT_LOSS_GOAL_ID)
                .orElseThrow(() -> new IllegalStateException("WEIGHT_LOSS goal not found"));
        testUser = createTestUser(TEST_USER_NAME, TEST_USER_EMAIL);
    }

    @Test
    void createDish_ShouldPersistWithAllFieldsAndRelations() {
        // Arrange
        Dish dish = createDishTemplate(DISH_1_NAME)
                .protein(DISH_1_PROTEIN)
                .fat(DISH_1_FAT)
                .carbohydrates(DISH_1_CARBS)
                .build();

        // Act
        Dish createdDish = dishService.createDish(testUser.getId(), dish);

        // Assert
        Dish dbDish = dishRepository.findById(createdDish.getId()).orElseThrow();
        assertPersistedDishEqualsRequest(dbDish, dish);
        assertUserRelation(dbDish);
        assertCreationTimestamp(dbDish);
    }

    @Test
    void createDish_ShouldEnforceUniqueNamePerUser() {
        // Arrange
        Dish firstDish = createDishTemplate(PASTA_NAME)
                .protein(PASTA_PROTEIN)
                .fat(PASTA_FAT)
                .carbohydrates(PASTA_CARBS)
                .build();

        dishService.createDish(testUser.getId(), firstDish);

        // Act & Assert
        Dish duplicateDish = createDishTemplate(PASTA_NAME).build();
        assertThatThrownBy(() -> dishService.createDish(testUser.getId(), duplicateDish))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    void getAllByUserId_ShouldReturnOnlyUsersDishes() {
        // Arrange
        User otherUser = createTestUser("Other User", OTHER_USER_EMAIL);

        Dish usersDish = createDish(testUser, "Dish 1");
        Dish otherUsersDish = createDish(otherUser, "Dish 2");

        // Act
        List<Dish> result = dishService.getAllByUserId(testUser.getId());

        // Assert
        assertThat(result)
                .extracting(Dish::getId)
                .containsExactly(usersDish.getId())
                .doesNotContain(otherUsersDish.getId());
    }

    @Test
    void createDish_ShouldAutoCalculateCaloriesWhenNotProvided() {
        // Arrange
        Dish dish = createDishTemplate(DISH_2_NAME)
                .protein(25.0)
                .fat(2.0)
                .carbohydrates(5.0)
                .build();

        // Act
        Dish createdDish = dishService.createDish(testUser.getId(), dish);

        // Assert
        int expectedCalories = CalorieCalculator.calculateDishCalories(dish);
        assertThat(createdDish.getCalories()).isEqualTo(expectedCalories);
    }

    private User createTestUser(String name, String email) {
        User user = User.builder()
                .name(name)
                .email(email)
                .age(30)
                .weight(70.5)
                .height(175)
                .gender(GenderEnum.MALE)
                .activityLevel(sedentaryActivityLevel)
                .goal(weighLossGoal)
                .build();
        user.setDailyCalorieTarget(CalorieCalculator.calculateDailyCalorieTarget(user));
        return userRepository.save(user);
    }

    private Dish.DishBuilder createDishTemplate(String name) {
        return Dish.builder()
                .name(name)
                .protein(null)
                .fat(null)
                .carbohydrates(null);
    }

    private Dish createDish(User user, String name) {
        return dishRepository.save(Dish.builder()
                .name(name)
                .user(user)
                .protein(20.0)
                .fat(5.0)
                .carbohydrates(30.0)
                .calories(250)
                .build());
    }

    private void assertPersistedDishEqualsRequest(Dish dbDish, Dish request) {
        assertThat(dbDish)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "user", "calories")
                .isEqualTo(request);
    }

    private void assertUserRelation(Dish dbDish) {
        assertThat(dbDish.getUser())
                .extracting(User::getId)
                .isEqualTo(testUser.getId());
    }

    private void assertCreationTimestamp(Dish dbDish) {
        assertThat(dbDish.getCreatedAt())
                .isBetween(Instant.now().minusSeconds(5), Instant.now());
    }

}
