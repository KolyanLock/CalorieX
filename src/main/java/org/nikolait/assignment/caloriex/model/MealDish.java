package org.nikolait.assignment.caloriex.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "meal_dishes")
public class MealDish {

    @EmbeddedId
    @ToString.Exclude
    @EqualsAndHashCode.Include
    private MealDishId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mealId")
    @JoinColumn(name = "meal_id")
    @ToString.Exclude
    private Meal meal;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("dishId")
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(nullable = false)
    private Double servings;

    public Double getCalories() {
        return Math.round(dish.getCalories() * servings * 100) / 100.0;
    }

}

