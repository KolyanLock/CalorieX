package org.nikolait.assignment.caloriex.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.nikolait.assignment.caloriex.model.ActivityLevel;
import org.nikolait.assignment.caloriex.model.Goal;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.repository.ActivityLevelRepository;
import org.nikolait.assignment.caloriex.repository.GoalRepository;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.nikolait.assignment.caloriex.service.UserService;
import org.nikolait.assignment.caloriex.ulti.CalorieCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ActivityLevelRepository activityLevelRepository;
    private final GoalRepository goalRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("User id must be null when creating");
        }

        requireNonNull(user.getActivityLevel(), "User activityLevel is required when creating");
        requireNonNull(user.getActivityLevel(), "User activityLevel id is required when creating");
        requireNonNull(user.getGoal(), "User goal is required when creating");
        requireNonNull(user.getGoal().getId(), "User goal id is required when creating");

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityAlreadyExistsException("User with email %s already exists.".formatted(user.getEmail()));
        }

        user.setName(StringUtils.normalizeSpace(user.getName()));

        user.setActivityLevel(getActivityLevelById(user.getActivityLevel().getId()));
        user.setGoal(getGoalById(user.getGoal().getId()));

        int dailyCalorieTarget = CalorieCalculator.calculateDailyCalorieTarget(user);
        user.setDailyCalorieTarget(dailyCalorieTarget);

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id %d was not found".formatted(id)
                ));
    }

    private ActivityLevel getActivityLevelById(Long id) {
        return activityLevelRepository.findById(id)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "ActivityLevel with id %d was not found".formatted(id)
                ));
    }

    private Goal getGoalById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "Goal with id %d was not found".formatted(id)
                ));
    }
}
