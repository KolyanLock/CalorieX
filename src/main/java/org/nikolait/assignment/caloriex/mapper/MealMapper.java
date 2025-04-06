package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.MealCreationDto;
import org.nikolait.assignment.caloriex.dto.MealResponseDto;
import org.nikolait.assignment.caloriex.model.Meal;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = MealDishMapper.class
)
public interface MealMapper {

    Meal toModel(MealCreationDto mealCreationDto);

    MealResponseDto toResponseDto(Meal meal);

    List<MealResponseDto> toResponseDtoList(List<Meal> mealList);

}
