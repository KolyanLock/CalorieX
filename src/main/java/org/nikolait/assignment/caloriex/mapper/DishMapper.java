package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.DishCreationDto;
import org.nikolait.assignment.caloriex.dto.DishResponseDto;
import org.nikolait.assignment.caloriex.model.Dish;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DishMapper {

    Dish toModel(DishCreationDto dto);

    DishResponseDto toResponseDto(Dish dish);

    List<DishResponseDto> toResponseDtoList(List<Dish> dishes);

}
