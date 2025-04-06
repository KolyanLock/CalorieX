package org.nikolait.assignment.caloriex.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.*;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.MealRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.MealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;

    @Override
    public Meal getUserMeal(Long userId, Long id) {
        return mealRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Meal with id %d was not found for current User".formatted(id)
                ));
    }

    @Override
    public List<Meal> getAllUserMeals(Long userId) {
        return mealRepository.findAllByUserId(userId);
    }

    @Override
    public List<Meal> getUserMealsForToday(Long userId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant start = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return mealRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    public List<Meal> getUserMealsForDay(Long userId, LocalDate day) {
        Instant start = day.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = day.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return mealRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    public List<Meal> getUserMealsBetween(Long userId, LocalDate startDay, LocalDate endDay) {
        Instant start = startDay.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return mealRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    @Transactional
    public Meal createMeal(Long userId, Meal meal) {
        if (meal.getId() != null) {
            throw new IllegalArgumentException("Meal id must be null when creating");
        }
        if (meal.getMealDishes() == null || meal.getMealDishes().isEmpty()) {
            throw new IllegalArgumentException("Meal must include at least one MealDish when creating");
        }

        meal.setUser(getUserById(userId));
        meal.setName(meal.getName() == null ? "" : StringUtils.normalizeSpace(meal.getName()));

        meal.getMealDishes().forEach(mealDish -> populateMealDish(meal, mealDish));

        return mealRepository.save(meal);

    }

    private void populateMealDish(Meal meal, MealDish mealDish) {
        mealDish.setId(new MealDishId());
        mealDish.setMeal(meal);

        requireNonNull(mealDish.getDish(), "Dish must be specified while creating a Meal");
        requireNonNull(mealDish.getDish().getId(), "Dish id must be specified while creating a Meal");
        requireNonNull(mealDish.getServings(), "Servings must be specified while creating a Meal");

        if (mealDish.getServings() <= 0) {
            throw new IllegalArgumentException("Servings must be greater than zero while creating a Meal");
        }

        mealDish.setDish(getDishById(mealDish.getDish().getId()));
    }

    private Dish getDishById(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "Dish with id %d was not found".formatted(id)
                ));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "User with id %d was not found".formatted(userId)
                ));
    }
}
