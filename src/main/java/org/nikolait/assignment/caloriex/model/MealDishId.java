package org.nikolait.assignment.caloriex.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MealDishId implements Serializable {
    private Long mealId;
    private Long dishId;
}
