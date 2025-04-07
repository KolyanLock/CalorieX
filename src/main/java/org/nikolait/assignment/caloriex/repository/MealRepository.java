package org.nikolait.assignment.caloriex.repository;

import org.nikolait.assignment.caloriex.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {

    Optional<Meal> findByIdAndUserId(Long id, Long userId);

    List<Meal> findAllByUserId(Long userId);

    List<Meal> findByUserIdAndCreatedAtBetweenOrderByCreatedAt(Long userId, Instant start, Instant end);

}
