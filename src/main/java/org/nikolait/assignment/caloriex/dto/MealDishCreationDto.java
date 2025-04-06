package org.nikolait.assignment.caloriex.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealDishCreationDto {

    @NotNull
    private Long dishId;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    private Double servings;

}
