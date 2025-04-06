package org.nikolait.assignment.caloriex.repository;

import org.nikolait.assignment.caloriex.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    Optional<Dish> findByIdAndUserId(Long id, Long userId);

    List<Dish> getAllByUserId(Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

}
