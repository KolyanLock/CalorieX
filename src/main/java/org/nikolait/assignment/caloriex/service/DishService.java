package org.nikolait.assignment.caloriex.service;

import org.nikolait.assignment.caloriex.model.Dish;

import java.util.List;

public interface DishService {

    Dish getUserDish(Long userId, Long id);

    Dish createDish(Long userId, Dish dish);

    List<Dish> getAllByUserId(Long userId);
}
