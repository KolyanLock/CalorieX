package org.nikolait.assignment.caloriex.unit;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.nikolait.assignment.caloriex.UnitTest;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.model.Dish;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.impl.DishServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DishServiceImplTest extends UnitTest {

    private static final Long USER_ID = 1L;
    private static final Long DISH_ID = 1L;
    private static final String DISH_NAME = "Test Dish";
    private static final Integer CALORIES = 500;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DishServiceImpl dishService;

    private User testUser;
    private Dish baseDish;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(USER_ID).build();
        baseDish = Dish.builder()
                .name(DISH_NAME)
                .calories(CALORIES)
                .protein(20.0)
                .fat(10.0)
                .carbohydrates(30.0)
                .build();
    }

    @Test
    void getUserDish_Success() {
        Dish expected = baseDish.toBuilder().id(DISH_ID).build();
        when(dishRepository.findByIdAndUserId(DISH_ID, USER_ID))
                .thenReturn(Optional.of(expected));

        Dish result = dishService.getUserDish(USER_ID, DISH_ID);

        assertThat(result).isEqualTo(expected);
        verify(dishRepository).findByIdAndUserId(DISH_ID, USER_ID);
    }

    @Test
    void getUserDish_NotFound_ThrowsEntityNotFoundException() {
        when(dishRepository.findByIdAndUserId(DISH_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> dishService.getUserDish(USER_ID, DISH_ID));

        verify(dishRepository).findByIdAndUserId(DISH_ID, USER_ID);
    }

    @Test
    void createDish_WithNonNullId_ThrowsIllegalArgumentException() {
        Dish invalidDish = baseDish.toBuilder().id(DISH_ID).build();

        assertThrows(IllegalArgumentException.class,
                () -> dishService.createDish(USER_ID, invalidDish));

        verifyNoInteractions(dishRepository, userRepository);
    }

    @Test
    void createDish_NormalizesName() {
        Dish request = baseDish.toBuilder().name("  Test  Dish  ").build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(DISH_NAME, USER_ID)).thenReturn(false);
        when(dishRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Dish result = dishService.createDish(USER_ID, request);

        assertThat(result.getName()).isEqualTo(DISH_NAME);
        verify(dishRepository).existsByNameAndUserId(DISH_NAME, USER_ID);
    }

    @Test
    void createDish_ExistingNameForUser_ThrowsEntityAlreadyExistsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(DISH_NAME, USER_ID)).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> dishService.createDish(USER_ID, baseDish));

        verify(dishRepository).existsByNameAndUserId(DISH_NAME, USER_ID);
        verify(dishRepository, never()).save(any());
    }

    @Test
    void createDish_CaloriesProvided_SavesWithProvidedCalories() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(any(), any())).thenReturn(false);
        when(dishRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Dish result = dishService.createDish(USER_ID, baseDish);

        assertThat(result.getCalories()).isEqualTo(CALORIES);
        verify(dishRepository).save(any());
    }

    @Test
    void createDish_CaloriesNull_MissingFields_ThrowsValidationException() {
        Dish invalidDish = baseDish.toBuilder()
                .calories(null)
                .fat(null)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(any(), any())).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> dishService.createDish(USER_ID, invalidDish));

        assertThat(ex.getMessage()).contains("fat");
        verify(dishRepository, never()).save(any());
    }

    @Test
    void createDish_CaloriesNull_CalculatesCalories() {
        Dish request = baseDish.toBuilder()
                .calories(null)
                .protein(30.0)
                .fat(10.0)
                .carbohydrates(5.0)
                .build();

        int expectedCalories = (30 * 4) + (10 * 9) + (5 * 4); // 230
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(any(), any())).thenReturn(false);
        when(dishRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Dish result = dishService.createDish(USER_ID, request);

        assertThat(result.getCalories()).isEqualTo(expectedCalories);
    }

    // 9. Пользователь не найден
    @Test
    void createDish_UserNotFound_ThrowsRuntimeException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> dishService.createDish(USER_ID, baseDish));

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(dishRepository);
    }

    @Test
    void createDish_AssignsUserToDish() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(any(), any())).thenReturn(false);
        when(dishRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Dish result = dishService.createDish(USER_ID, baseDish);

        assertThat(result.getUser())
                .isNotNull()
                .extracting(User::getId)
                .isEqualTo(USER_ID);

        ArgumentCaptor<Dish> dishCaptor = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepository).save(dishCaptor.capture());

        assertThat(dishCaptor.getValue().getUser())
                .isNotNull()
                .extracting(User::getId)
                .isEqualTo(USER_ID);
    }

    @Test
    void getAllByUserId_ReturnsDishesFromRepository() {
        List<Dish> expected = List.of(
                baseDish.toBuilder().id(1L).build(),
                baseDish.toBuilder().id(2L).build()
        );
        when(dishRepository.getAllByUserId(USER_ID)).thenReturn(expected);

        List<Dish> result = dishService.getAllByUserId(USER_ID);

        assertThat(result).isEqualTo(expected);
        verify(dishRepository).getAllByUserId(USER_ID);
    }

    @Test
    void createDish_AutoPopulatesCreatedAt() {
        // Arrange: Mock dependencies
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(dishRepository.existsByNameAndUserId(any(), any())).thenReturn(false);

        // Mock save operation to return dish WITH populated createdAt
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish dishToSave = invocation.getArgument(0);
            return dishToSave.toBuilder()
                    .createdAt(Instant.now()) // Simulate DB auto-population
                    .build();
        });

        // Act: Call service method
        Dish result = dishService.createDish(USER_ID, baseDish);

        // Assert: Verify timestamp
        assertThat(result.getCreatedAt())
                .as("CreatedAt should be auto-populated")
                .isNotNull()
                .isBetween(Instant.now().minusSeconds(2), Instant.now());
    }
}
