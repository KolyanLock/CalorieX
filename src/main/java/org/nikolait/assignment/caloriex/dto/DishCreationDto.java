package org.nikolait.assignment.caloriex.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishCreationDto {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Min(0)
    @Max(1000)
    @Schema(description = "grams")
    private Double protein;

    @Min(0)
    @Max(1000)
    @Schema(description = "grams")
    private Double fat;

    @Min(0)
    @Max(1000)
    @Schema(description = "grams")
    private Double carbohydrates;

    @Min(1)
    @Max(1000)
    private Integer calories;

}
