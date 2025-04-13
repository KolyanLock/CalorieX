package org.nikolait.assignment.caloriex.unit;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.nikolait.assignment.caloriex.UnitTests;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.ActivityLevel;
import org.nikolait.assignment.caloriex.model.GenderEnum;
import org.nikolait.assignment.caloriex.model.Goal;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.repository.ActivityLevelRepository;
import org.nikolait.assignment.caloriex.repository.GoalRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.impl.UserServiceImpl;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest extends UnitTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ActivityLevelRepository activityLevelRepository;
    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_NAME = "Test User";
    private static final int TEST_AGE = 30;
    private static final double TEST_WEIGHT = 60.0;  // Updated weight
    private static final int TEST_HEIGHT = 170;      // Updated height
    private static final GenderEnum TEST_GENDER = GenderEnum.MALE;

    private ActivityLevel defaultActivityLevel;
    private Goal defaultGoal;
    private User validUser;

    @BeforeEach
    void setUp() {
        defaultActivityLevel = ActivityLevel.builder()
                .id(1L)
                .name("Active")
                .multiplier(1.5)
                .build();

        defaultGoal = Goal.builder()
                .id(1L)
                .name("Gain")
                .multiplier(1.2)
                .build();

        validUser = User.builder()
                .name(TEST_NAME)
                .email(TEST_EMAIL)
                .age(TEST_AGE)
                .weight(TEST_WEIGHT)
                .height(TEST_HEIGHT)
                .gender(TEST_GENDER)
                .activityLevel(ActivityLevel.builder().id(1L).build())
                .goal(Goal.builder().id(1L).build())
                .build();
    }

    @Test
    void createUser_WhenUserIdIsNotNull_ThrowsException() {
        User user = validUser.toBuilder()
                .id(1L)
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    @Test
    void createUser_WhenEmailExists_ThrowsException() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);
        assertThrows(EntityAlreadyExistsException.class, () -> userService.createUser(validUser));
    }

    @Test
    void createUser_WhenNameHasExtraSpaces_NormalizesName() {
        mockSuccessfulUserCreationDependencies();

        User result = userService.createUser(validUser.toBuilder()
                .name("  Test  User  ")
                .build());

        assertThat(result.getName()).isEqualTo(TEST_NAME);
    }

    @Test
    void createUser_WhenValidUser_SetsDependenciesCorrectly() {
        // Setup required mocks
        mockSuccessfulUserCreationDependencies();

        User result = userService.createUser(validUser);

        assertThat(result.getActivityLevel()).isEqualTo(defaultActivityLevel);
        assertThat(result.getGoal()).isEqualTo(defaultGoal);
    }

    @ParameterizedTest
    @EnumSource(GenderEnum.class)
    void createUser_WhenValidUser_CalculatesCorrectCalorieTargetForGender(GenderEnum gender) {
        mockSuccessfulUserCreationDependencies();

        User testUser = validUser.toBuilder()
                .gender(gender)
                .build();

        // Harris-Benedict calculation for test data:
        // Weight: 60 kg, Height: 170 cm, Age: 30
        // Activity: 1.5, Goal: 1.2
        int expectedCalories = switch (gender) {
            case MALE -> (int) Math.round(
                    (88.36 + (13.4 * 60) + (4.8 * 170) - (5.7 * 30)) * 1.5 * 1.2
            );  // 2658 calories
            case FEMALE -> (int) Math.round(
                    (447.6 + (9.2 * 60) + (3.1 * 170) - (4.3 * 30)) * 1.5 * 1.2
            );  // 2343 calories
        };

        User result = userService.createUser(testUser);
        assertThat(result.getDailyCalorieTarget()).isEqualTo(expectedCalories);
    }

    @Test
    void createUser_WhenActivityLevelNotFound_ThrowsException() {
        when(activityLevelRepository.findById(999L)).thenReturn(Optional.empty());

        User user = validUser.toBuilder()
                .activityLevel(ActivityLevel.builder().id(999L).build())
                .build();

        assertThrows(UnprocessableEntityException.class, () -> userService.createUser(user));
    }

    @Test
    void getUserById_WhenUserExists_ReturnsUser() {
        User expectedUser = validUser.toBuilder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        User result = userService.getUserById(1L);
        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    void getUserById_WhenUserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void createUser_WhenGoalNotFound_ThrowsException() {
        when(activityLevelRepository.findById(1L)).thenReturn(Optional.of(defaultActivityLevel));
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        User user = validUser.toBuilder()
                .goal(Goal.builder().id(999L).build())
                .build();

        assertThrows(UnprocessableEntityException.class, () -> userService.createUser(user));
    }

    @Test
    void createUser_WhenValidUser_SavesWithCreationTimestamp() {
        // Setup required mocks
        mockSuccessfulActivityLevelAndGoalLookup();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setCreatedAt(Instant.now());
            return userToSave;
        });

        userService.createUser(validUser);

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getCreatedAt())
                .isNotNull()
                .isBetween(Instant.now().minusSeconds(5), Instant.now());
    }

    private void mockSuccessfulUserCreationDependencies() {
        mockSuccessfulActivityLevelAndGoalLookup();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockSuccessfulActivityLevelAndGoalLookup() {
        when(activityLevelRepository.findById(1L)).thenReturn(Optional.of(defaultActivityLevel));
        when(goalRepository.findById(1L)).thenReturn(Optional.of(defaultGoal));
    }

}
