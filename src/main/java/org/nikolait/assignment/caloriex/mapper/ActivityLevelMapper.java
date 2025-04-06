package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.ActivityLevelResponseDto;
import org.nikolait.assignment.caloriex.model.ActivityLevel;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActivityLevelMapper {

    ActivityLevelResponseDto toResponseDto(ActivityLevel activityLevel);

    List<ActivityLevelResponseDto> toResponseDtoList(List<ActivityLevel> activityLevels);
}
