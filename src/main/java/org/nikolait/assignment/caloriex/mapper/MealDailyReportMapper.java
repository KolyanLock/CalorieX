package org.nikolait.assignment.caloriex.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.nikolait.assignment.caloriex.dto.MealDailyReportDto;
import org.nikolait.assignment.caloriex.model.MealDailyReport;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = MealMapper.class
)
public interface MealDailyReportMapper {

    MealDailyReportDto toResponseDto(MealDailyReport mealDailyReport);

    List<MealDailyReportDto> toResponseDtoList(List<MealDailyReport> mealDailyReports);

}
