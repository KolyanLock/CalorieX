package org.nikolait.assignment.caloriex.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikolait.assignment.caloriex.model.GenderEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDto {

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Min(18)
    @Max(150)
    private Integer age;

    @NotNull
    @Min(30)
    @Max(300)
    @Schema(description = "kilograms")
    private Double weight;

    @NotNull
    @Min(50)
    @Max(300)
    @Schema(description = "centimeters", example = "170")
    private Integer height;

    @NotNull
    private GenderEnum gender;

    @NotNull
    @Schema(example = "1")
    private Long activityLevelId;

    @NotNull
    @Schema(example = "1")
    private Long goalId;

}
