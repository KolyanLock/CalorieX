package org.nikolait.assignment.caloriex.integration;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.nikolait.assignment.caloriex.IntegrationTestBase;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.ActivityLevel;
import org.nikolait.assignment.caloriex.model.GenderEnum;
import org.nikolait.assignment.caloriex.model.Goal;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.service.UserService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest extends IntegrationTestBase {

    // Reference data from migrations
    private static final Long SEDENTARY_ACTIVITY_ID = 1L;
    private static final Long MODERATE_ACTIVITY_ID = 3L;
    private static final Long MAINTENANCE_GOAL_ID = 2L;
    private static final Long MUSCLE_GAIN_GOAL_ID = 3L;
    private static final Long INVALID_ID = 999999999L;

    // Test data constants
    private static final String TEST_NAME = "Test User";
    private static final String TEST_EMAIL_PREFIX = "test.user";
    private static final int TEST_AGE = 30;
    private static final double TEST_WEIGHT = 60.0;
    private static final int TEST_HEIGHT = 170;

    @Autowired
    private UserService userService;

    private User testUser;
    private ActivityLevel moderateActivity;
    private Goal muscleGainGoal;

    @BeforeEach
    void setUp() {
        // Load reference entities from DB
        moderateActivity = activityLevelRepository.findById(MODERATE_ACTIVITY_ID).orElseThrow();
        muscleGainGoal = goalRepository.findById(MUSCLE_GAIN_GOAL_ID).orElseThrow();

        // Initialize base user with unique email
        testUser = User.builder()
                .name(TEST_NAME)
                .email(generateUniqueEmail())
                .age(TEST_AGE)
                .weight(TEST_WEIGHT)
                .height(TEST_HEIGHT)
                .gender(GenderEnum.MALE)
                .activityLevel(moderateActivity)
                .goal(muscleGainGoal)
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldPersistAllFields() {
        // Act
        User createdUser = userService.createUser(testUser);

        // Assert
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getCreatedAt())
                .isBetween(Instant.now().minusSeconds(5), Instant.now());

        verifyPersistedData(createdUser);
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        User firstUser = userService.createUser(testUser);

        // Act & Assert
        User duplicateUser = buildUserWithSameEmail(firstUser.getEmail());
        assertThrows(EntityAlreadyExistsException.class,
                () -> userService.createUser(duplicateUser));
    }

    @Test
    void createUser_WithInvalidActivityLevel_ShouldThrowException() {
        // Arrange
        User invalidUser = buildUserWithInvalidActivityLevel();

        // Act & Assert
        assertThrows(UnprocessableEntityException.class,
                () -> userService.createUser(invalidUser));
    }

    @Test
    void createUser_WithInvalidGoal_ShouldThrowException() {
        // Arrange
        User invalidUser = buildUserWithInvalidGoal();

        // Act & Assert
        assertThrows(UnprocessableEntityException.class,
                () -> userService.createUser(invalidUser));
    }

    @ParameterizedTest
    @EnumSource(GenderEnum.class)
    void createUser_ShouldCalculateCaloriesUsingCalculator(GenderEnum gender) {
        // Arrange
        User user = buildUserForGenderTest(gender);

        // Act
        User createdUser = userService.createUser(user);

        // Assert
        int expectedCalories = CalorieCalculator.calculateDailyCalorieTarget(user);
        assertThat(createdUser.getDailyCalorieTarget())
                .isEqualTo(expectedCalories);
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() {
        // Arrange
        User expected = userService.createUser(testUser);

        // Act
        User actual = userService.getUserById(expected.getId());

        // Assert
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> userService.getUserById(INVALID_ID));
    }

    // region Helper Methods
    private void verifyPersistedData(User createdUser) {
        User dbUser = userRepository.findById(createdUser.getId()).orElseThrow();

        assertThat(dbUser).usingRecursiveComparison()
                .ignoringFields("createdAt", "activityLevel", "goal")
                .isEqualTo(createdUser);

        assertThat(dbUser.getActivityLevel().getId())
                .isEqualTo(MODERATE_ACTIVITY_ID);
        assertThat(dbUser.getGoal().getId())
                .isEqualTo(MUSCLE_GAIN_GOAL_ID);
    }

    private User buildUserWithSameEmail(String email) {
        return User.builder()
                .name("Different Name")
                .email(email)
                .age(25)
                .weight(65.0)
                .height(175)
                .gender(GenderEnum.FEMALE)
                .activityLevel(moderateActivity)
                .goal(muscleGainGoal)
                .build();
    }

    private User buildUserWithInvalidActivityLevel() {
        return testUser.toBuilder()
                .activityLevel(ActivityLevel.builder().id(INVALID_ID).build())
                .build();
    }

    private User buildUserWithInvalidGoal() {
        return testUser.toBuilder()
                .goal(Goal.builder().id(INVALID_ID).build())
                .build();
    }

    private User buildUserForGenderTest(GenderEnum gender) {
        return testUser.toBuilder()
                .email(generateUniqueEmail())
                .gender(gender)
                .activityLevel(activityLevelRepository.findById(SEDENTARY_ACTIVITY_ID).orElseThrow())
                .goal(goalRepository.findById(MAINTENANCE_GOAL_ID).orElseThrow())
                .build();
    }

    private String generateUniqueEmail() {
        return TEST_EMAIL_PREFIX + UUID.randomUUID() + "@example.com";
    }

}
