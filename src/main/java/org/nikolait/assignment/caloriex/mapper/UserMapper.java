package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.UserCreationDto;
import org.nikolait.assignment.caloriex.dto.UserResponseDto;
import org.nikolait.assignment.caloriex.model.User;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ActivityLevelMapper.class, GoalMapper.class}
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dailyCalorieTarget", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "activityLevelId", target = "activityLevel.id")
    @Mapping(source = "goalId", target = "goal.id")
    User toModel(UserCreationDto dto);

    UserResponseDto toResponseDto(User user);

}
