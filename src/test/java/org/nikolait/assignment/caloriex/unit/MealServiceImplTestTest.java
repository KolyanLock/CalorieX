package org.nikolait.assignment.caloriex.unit;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.nikolait.assignment.caloriex.UnitTests;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.MealRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.impl.MealServiceImpl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MealServiceImplTestTest extends UnitTests {

    // Test constants
    private static final Long USER_ID = 1L;
    private static final Long MEAL_ID = 100L;
    private static final Long DISH_ID = 200L;
    private static final Long NON_EXISTENT_DISH_ID = 999L;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private static final ZoneId EUROPE_PARIS = ZoneId.of("Europe/Paris");
    private static final ZoneId UTC = ZoneId.of("UTC");

    // Test entities
    private User testUser;
    private Dish testDish;

    @Mock
    private MealRepository mealRepository;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealServiceImpl mealService;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(USER_ID).build();
        testDish = Dish.builder()
                .id(DISH_ID)
                .calories(300)
                .build();

        // Configure common lenient mocks
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        lenient().when(dishRepository.findById(DISH_ID)).thenReturn(Optional.of(testDish));
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verify(userRepository, atMostOnce()).findById(USER_ID);
        verify(dishRepository, atMostOnce()).findById(DISH_ID);
        verifyNoMoreInteractions(mealRepository, dishRepository, userRepository);
    }

    @Nested
    @DisplayName("Meal Creation Tests")
    class MealCreationTests {

        @Test
        @DisplayName("Should successfully create meal with valid input")
        void createMeal_validInput_returnsSavedMeal() {
            // Setup test data
            MealDish validMealDish = new MealDish(
                    new MealDishId(),
                    null,  // meal reference will be set during save
                    testDish,
                    2.0
            );

            Meal newMeal = Meal.builder()
                    .mealDishes(new ArrayList<>(Collections.singletonList(validMealDish)))
                    .build();

            // Configure mock behaviors
            when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
                Meal savedMeal = invocation.getArgument(0);
                // Simulate JPA relationship management
                savedMeal.getMealDishes().forEach(md -> md.setMeal(savedMeal));
                return savedMeal;
            });

            // Execute service method
            Meal result = mealService.createMeal(USER_ID, newMeal);

            // Verify results
            assertNotNull(result, "Saved meal should not be null");
            assertEquals(testUser, result.getUser(), "Meal should be associated with correct user");
            assertEquals(1, result.getMealDishes().size(), "Meal should contain exactly one dish");

            MealDish resultMealDish = result.getMealDishes().getFirst();
            assertNotNull(resultMealDish.getId(), "MealDish should have generated ID");
            assertEquals(result, resultMealDish.getMeal(), "MealDish should reference parent meal");
            assertEquals(testDish, resultMealDish.getDish(), "MealDish should reference correct dish");

            // Verify interactions
            verify(mealRepository).save(newMeal);
            verify(dishRepository).findById(DISH_ID);
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0})
        @DisplayName("Should throw exception when servings are invalid")
        void createMeal_invalidServings_throwsException(double invalidServings) {
            // Setup test data with invalid servings
            MealDish invalidMealDish = new MealDish(
                    new MealDishId(),
                    null,
                    testDish,
                    invalidServings
            );

            Meal invalidMeal = Meal.builder()
                    .mealDishes(List.of(invalidMealDish))
                    .build();

            // Execute & verify exception
            assertThrows(IllegalArgumentException.class,
                    () -> mealService.createMeal(USER_ID, invalidMeal),
                    "Should reject meal with invalid servings");

            // Verify no dish lookup occurred
            verifyNoInteractions(dishRepository);
        }

        @Test
        @DisplayName("Should throw exception when referenced dish doesn't exist")
        void createMeal_nonExistentDish_throwsException() {
            // Setup test data with unknown dish
            Dish unknownDish = Dish.builder().id(NON_EXISTENT_DISH_ID).build();
            MealDish invalidMealDish = new MealDish(
                    new MealDishId(),
                    null,
                    unknownDish,
                    2.0
            );

            Meal invalidMeal = Meal.builder()
                    .mealDishes(List.of(invalidMealDish))
                    .build();

            // Configure mock to return empty optional
            when(dishRepository.findById(NON_EXISTENT_DISH_ID)).thenReturn(Optional.empty());

            // Execute & verify exception
            Exception exception = assertThrows(UnprocessableEntityException.class,
                    () -> mealService.createMeal(USER_ID, invalidMeal));

            assertTrue(exception.getMessage().contains(
                            String.format("Dish with id %d was not found", NON_EXISTENT_DISH_ID)),
                    "Exception message should reference missing dish");

            // Verify dish lookup attempt
            verify(dishRepository).findById(NON_EXISTENT_DISH_ID);
        }

        @Test
        @DisplayName("Should normalize meal name whitespace")
        void createMeal_normalizesNameWhitespace() {
            // Setup test data with messy name
            MealDish validMealDish = new MealDish(
                    new MealDishId(),
                    null,
                    testDish,
                    2.0
            );

            Meal newMeal = Meal.builder()
                    .name("  Breakfast Special   ")
                    .mealDishes(new ArrayList<>(Collections.singletonList(validMealDish)))
                    .build();

            // Configure mock save behavior
            when(mealRepository.save(any(Meal.class))).thenAnswer(invocation -> {
                Meal savedMeal = invocation.getArgument(0);
                savedMeal.getMealDishes().forEach(md -> md.setMeal(savedMeal));
                return savedMeal;
            });

            // Execute service method
            Meal result = mealService.createMeal(USER_ID, newMeal);

            // Verify name normalization
            assertEquals("Breakfast Special", result.getName(),
                    "Should trim and normalize whitespace in meal name");

            // Verify interactions
            verify(mealRepository).save(any(Meal.class));
            verify(dishRepository).findById(DISH_ID);
        }
    }

    @Nested
    @DisplayName("Meal Retrieval Tests")
    class MealRetrievalTests {

        @Test
        @DisplayName("Should retrieve existing meal by ID")
        void getUserMeal_existingMeal_returnsMeal() {
            // Setup mock data
            Meal expectedMeal = new Meal();
            when(mealRepository.findByIdAndUserId(MEAL_ID, USER_ID))
                    .thenReturn(Optional.of(expectedMeal));

            // Execute service method
            Meal result = mealService.getUserMeal(USER_ID, MEAL_ID);

            // Verify results
            assertEquals(expectedMeal, result, "Should return found meal");
            verify(mealRepository).findByIdAndUserId(MEAL_ID, USER_ID);
        }

        @Test
        @DisplayName("Should throw exception for non-existent meal ID")
        void getUserMeal_nonExistentMeal_throwsException() {
            // Configure mock to return empty
            when(mealRepository.findByIdAndUserId(MEAL_ID, USER_ID))
                    .thenReturn(Optional.empty());

            // Execute & verify exception
            assertThrows(EntityNotFoundException.class,
                    () -> mealService.getUserMeal(USER_ID, MEAL_ID),
                    "Should throw when meal not found");
        }

        @Test
        @DisplayName("Should retrieve all user meals")
        void getAllUserMeals_returnsAllMeals() {
            // Setup mock data
            List<Meal> expectedMeals = List.of(new Meal(), new Meal());
            when(mealRepository.findAllByUserId(USER_ID)).thenReturn(expectedMeals);

            // Execute service method
            List<Meal> result = mealService.getAllUserMeals(USER_ID);

            // Verify results
            assertEquals(expectedMeals.size(), result.size(),
                    "Should return all user meals");
            verify(mealRepository).findAllByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("Meal Filtering Tests")
    class MealFilteringTests {

        @Test
        @DisplayName("Should filter meals by specific date")
        void getUserMealsForDay_returnsDailyMeals() {
            // Calculate date range
            Instant start = TEST_DATE.atStartOfDay(EUROPE_PARIS).toInstant();
            Instant end = TEST_DATE.plusDays(1).atStartOfDay(EUROPE_PARIS).toInstant();

            // Setup mock data
            List<Meal> expectedMeals = List.of(new Meal());
            when(mealRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAt(USER_ID, start, end))
                    .thenReturn(expectedMeals);

            // Execute service method
            List<Meal> result = mealService.getUserMealsForDay(USER_ID, TEST_DATE, EUROPE_PARIS);

            // Verify results
            assertEquals(expectedMeals.size(), result.size(),
                    "Should return meals for specified day");
            verify(mealRepository).findByUserIdAndCreatedAtBetweenOrderByCreatedAt(USER_ID, start, end);
        }

        @Test
        @DisplayName("Should filter meals by date range")
        void getUserMealsBetween_returnsDateRangeMeals() {
            // Define date range
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            Instant startInstant = startDate.atStartOfDay(UTC).toInstant();
            Instant endInstant = endDate.plusDays(1).atStartOfDay(UTC).toInstant();

            // Setup mock data
            List<Meal> expectedMeals = List.of(new Meal(), new Meal());
            when(mealRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAt(USER_ID, startInstant, endInstant))
                    .thenReturn(expectedMeals);

            // Execute service method
            List<Meal> result = mealService.getUserMealsBetween(USER_ID, startDate, endDate, UTC);

            // Verify results
            assertEquals(expectedMeals.size(), result.size(),
                    "Should return meals within date range");
            verify(mealRepository).findByUserIdAndCreatedAtBetweenOrderByCreatedAt(USER_ID, startInstant, endInstant);
        }
    }
}
