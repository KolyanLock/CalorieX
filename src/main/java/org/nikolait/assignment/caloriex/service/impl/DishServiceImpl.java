package org.nikolait.assignment.caloriex.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.Dish;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.repository.DishRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.DishService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final UserRepository userRepository;

    @Override
    public Dish getUserDish(Long userId, Long id) {
        return dishRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Dish with id %d was not found for current User".formatted(id)
                ));
    }

    @Override
    @Transactional
    public Dish createDish(Long userId, Dish dish) {
        if (dish.getId() != null) {
            throw new IllegalArgumentException("Dish id must be null when creating");
        }
        dish.setName((StringUtils.normalizeSpace(dish.getName())));
        if (dishRepository.existsByNameAndUserId(dish.getName(), userId)) {
            throw new EntityAlreadyExistsException(
                    "Dish with name %s already exists for current User".formatted(dish.getName())
            );
        }
        dish.setUser(getUserById(userId));
        if (dish.getCalories() == null) {
            validateDishComposition(dish);
            int caloriesPerServing = CalorieCalculator.calculateDishCalories(dish);
            dish.setCalories(caloriesPerServing);
            return dishRepository.save(dish);
        }
        return dishRepository.save(dish);
    }

    @Override
    public List<Dish> getAllByUserId(Long userId) {
        return dishRepository.getAllByUserId(userId);
    }

    private void validateDishComposition(Dish dish) {
        List<String> missingFields = Stream.of(
                        new AbstractMap.SimpleEntry<>("protein", dish.getProtein()),
                        new AbstractMap.SimpleEntry<>("fat", dish.getFat()),
                        new AbstractMap.SimpleEntry<>("carbohydrates", dish.getCarbohydrates())
                ).filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey).toList();

        if (!missingFields.isEmpty()) {
            throw new ValidationException(
                    "Missing fields required to calculate caloriesPerServing: " +
                            String.join(", ", missingFields)
            );
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "User with id %d not found".formatted(userId)
                ));
    }

}
