package org.nikolait.assignment.caloriex.repository;

import org.nikolait.assignment.caloriex.model.MealDish;
import org.nikolait.assignment.caloriex.model.MealDishId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealDishRepository extends JpaRepository<MealDish, MealDishId> {
}
