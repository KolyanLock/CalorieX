package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.GoalResponseDto;
import org.nikolait.assignment.caloriex.model.Goal;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GoalMapper {

    GoalResponseDto toResponseDto(Goal goal);

    List<GoalResponseDto> toResponseDtoList(List<Goal> goals);

}
