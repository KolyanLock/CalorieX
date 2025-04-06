package org.nikolait.assignment.caloriex.ulti;

import lombok.experimental.UtilityClass;
import org.nikolait.assignment.caloriex.model.Dish;
import org.nikolait.assignment.caloriex.model.User;

@UtilityClass
public class CalorieCalculator {

    // Constants for calculating calories from macronutrients
    private static final double PROTEIN_CALORIE_COEFFICIENT = 4.0;
    private static final double FAT_CALORIE_COEFFICIENT = 9.0;
    private static final double CARBOHYDRATE_CALORIE_COEFFICIENT = 4.0;

    // Constants for the Harris-Benedict formula
    private static final double MALE_BASE = 88.36;
    private static final double FEMALE_BASE = 447.6;
    private static final double MALE_WEIGHT_FACTOR = 13.4;
    private static final double FEMALE_WEIGHT_FACTOR = 9.2;
    private static final double MALE_HEIGHT_FACTOR = 4.8;
    private static final double FEMALE_HEIGHT_FACTOR = 3.1;
    private static final double MALE_AGE_FACTOR = 5.7;
    private static final double FEMALE_AGE_FACTOR = 4.3;

    /**
     * Calculates the calories per serving of a dish based on its macronutrient content.
     *
     * @param dish the Dish object containing the macronutrient values:
     *             protein (grams), fat (grams), and carbohydrates (grams).
     * @return the calculated calories per serving.
     */
    public int calculateDishCalories(Dish dish) {
        double calories = (dish.getProtein() * PROTEIN_CALORIE_COEFFICIENT) +
                (dish.getFat() * FAT_CALORIE_COEFFICIENT) +
                (dish.getCarbohydrates() * CARBOHYDRATE_CALORIE_COEFFICIENT);
        return (int) Math.round(calories);
    }

    /**
     * Calculates the daily calorie target for a user based on their
     * gender, weight, height, age, activity level, and goal.
     *
     * @param user The user object containing the necessary parameters for the calculation.
     * @return The calculated daily calorie target.
     */
    public static int calculateDailyCalorieTarget(User user) {
        double bmr = switch (user.getGender()) {
            case MALE -> MALE_BASE
                    + (MALE_WEIGHT_FACTOR * user.getWeight())
                    + (MALE_HEIGHT_FACTOR * user.getHeight())
                    - (MALE_AGE_FACTOR * user.getAge());
            case FEMALE -> FEMALE_BASE
                    + (FEMALE_WEIGHT_FACTOR * user.getWeight())
                    + (FEMALE_HEIGHT_FACTOR * user.getHeight())
                    - (FEMALE_AGE_FACTOR * user.getAge());
        };

        return (int) Math.round(bmr * user.getActivityLevel().getMultiplier() * user.getGoal().getMultiplier());
    }

}
