package org.nikolait.assignment.caloriex.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.nikolait.assignment.caloriex.UnitTests;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalorieCalculatorTest extends UnitTests {

    @Test
    void calculateDishCalories_shouldReturnCorrectValue_forTypicalMacros() {
        // Create a dish with typical macronutrient values
        Dish dish = Dish.builder()
                .protein(20.0)        // grams
                .fat(10.0)            // grams
                .carbohydrates(30.0)  // grams
                .build();

        // Expected calories calculation:
        // (20 * 4.0) + (10 * 9.0) + (30 * 4.0) = 80 + 90 + 120 = 290
        int expectedCalories = 290;

        int calculatedCalories = CalorieCalculator.calculateDishCalories(dish);

        assertEquals(expectedCalories, calculatedCalories);
    }

    @Test
    void calculateDishCalories_shouldRoundCorrectly_forFractionalValues() {
        // Create a dish with fractional macronutrient values
        Dish dish = Dish.builder()
                .protein(10.5)        // 10.5 grams of protein
                .fat(5.3)             // 5.3 grams of fat
                .carbohydrates(15.7)  // 15.7 grams of carbohydrates
                .build();

        // Expected calories calculation:
        // (10.5 * 4.0) + (5.3 * 9.0) + (15.7 * 4.0) = 42 + 47.7 + 62.8 = 152.5
        // After rounding, it should be 153 calories.
        int expectedCalories = 153;

        int calculatedCalories = CalorieCalculator.calculateDishCalories(dish);

        assertEquals(expectedCalories, calculatedCalories);
    }

    @Test
    void calculateDishCalories_shouldReturnZero_whenAllMacrosAreZero() {
        // Create a dish with zero macronutrient values
        Dish dish = Dish.builder()
                .protein(0.0)
                .fat(0.0)
                .carbohydrates(0.0)
                .build();

        int expectedCalories = 0;

        int calculatedCalories = CalorieCalculator.calculateDishCalories(dish);

        assertEquals(expectedCalories, calculatedCalories);
    }

    @ParameterizedTest
    @EnumSource(GenderEnum.class)
    void calculateDailyCalorieTarget_ValidUser_ReturnsCorrectValue(GenderEnum gender) {
        User testUser = buildTestUser(gender);

        int expectedCalories = calculateExpectedCalories(gender);

        int result = CalorieCalculator.calculateDailyCalorieTarget(testUser);

        assertEquals(expectedCalories, result);
    }

    private User buildTestUser(GenderEnum gender) {
        return switch (gender) {
            case MALE -> User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .age(30)
                    .weight(80.0)
                    .height(180)
                    .gender(GenderEnum.MALE)
                    .activityLevel(ActivityLevel.builder().multiplier(1.2).build())
                    .goal(Goal.builder().multiplier(0.9).build())
                    .build();

            case FEMALE -> User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .age(25)
                    .weight(65.0)
                    .height(165)
                    .gender(GenderEnum.FEMALE)
                    .activityLevel(ActivityLevel.builder().multiplier(1.3).build())
                    .goal(Goal.builder().multiplier(1.1).build())
                    .build();
        };
    }

    private int calculateExpectedCalories(GenderEnum gender) {
        return switch (gender) {
            case MALE -> {
                double bmr = 88.36 + (13.4 * 80.0) + (4.8 * 180) - (5.7 * 30);
                yield (int) Math.round(bmr * 1.2 * 0.9); // 2451 calories
            }
            case FEMALE -> {
                double bmr = 447.6 + (9.2 * 65.0) + (3.1 * 165) - (4.3 * 25);
                yield (int) Math.round(bmr * 1.3 * 1.1); // 2609 calories
            }
        };
    }
}
