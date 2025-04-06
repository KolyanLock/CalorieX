package org.nikolait.assignment.caloriex.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealCreationDto {

    @Size(min = 1, max = 255)
    private String name;

    @NotEmpty
    private List<MealDishCreationDto> mealDishes;

}
