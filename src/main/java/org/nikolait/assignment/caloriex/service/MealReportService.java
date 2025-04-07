package org.nikolait.assignment.caloriex.service;

import org.nikolait.assignment.caloriex.model.MealDailyReport;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface MealReportService {

    MealDailyReport generateMealDailyReportForToday(Long userId, ZoneId zoneId);

    MealDailyReport generateMealDailyReportForDay(Long userId, LocalDate day, ZoneId zoneId);

    List<MealDailyReport> generateMealDailyReportsForPeriod(
            Long userId,
            LocalDate startDay,
            LocalDate endDay,
            ZoneId zoneId
    );

    List<MealDailyReport> generateAllTrackedMealDailyReports(Long userId, ZoneId zoneId);

}
