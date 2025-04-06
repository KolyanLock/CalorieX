package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.MealDishCreationDto;
import org.nikolait.assignment.caloriex.dto.MealDishResponseDto;
import org.nikolait.assignment.caloriex.model.MealDish;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MealDishMapper {

    @Mapping(source = "dishId", target = "dish.id")
    MealDish toModel(MealDishCreationDto mealDishCreationDto);

    List<MealDish> toModelList(List<MealDishCreationDto> mealDishCreationDtoList);

    MealDishResponseDto toResponseDto(MealDish mealDish);

    List<MealDishResponseDto> toResponseDtoList(List<MealDish> mealDishList);

}
