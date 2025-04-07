package org.nikolait.assignment.caloriex.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.MealDailyReportDto;
import org.nikolait.assignment.caloriex.mapper.MealDailyReportMapper;
import org.nikolait.assignment.caloriex.model.MealDailyReport;
import org.nikolait.assignment.caloriex.secutiry.AuthorizationService;
import org.nikolait.assignment.caloriex.service.MealReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/meals/report/")
@RequiredArgsConstructor
public class MealReportController {

    private final AuthorizationService authorizationService;
    private final MealReportService mealReportService;
    private final MealDailyReportMapper mealDailyReportMapper;

    @GetMapping("/daily/today")
    @Operation(
            summary = "Get daily Meal report for the authenticated User for today",
            description = """
                     Requires User ID in the Authorization header <br>
                     Uses 'X-Time-Zone' header to determine user's time zone (default: UTC)
                    """,
            security = @SecurityRequirement(name = "Authorization")
    )
    public MealDailyReportDto getMealDailyReportForToday(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @RequestHeader(value = "X-Time-Zone", defaultValue = "UTC") ZoneId zoneId
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        MealDailyReport mealDailyReport = mealReportService.generateMealDailyReportForToday(userId, zoneId);

        return mealDailyReportMapper.toResponseDto(mealDailyReport);
    }

    @GetMapping("/daily/day")
    @Operation(
            summary = "Get daily Meal report for the authenticated User for a specific date",
            description = """
                     Requires User ID in the Authorization header <br>
                     Uses 'X-Time-Zone' header to determine user's time zone (default: UTC)
                    """,
            security = @SecurityRequirement(name = "Authorization")
    )
    public MealDailyReportDto getDailyMealReportForDate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @RequestHeader(value = "X-Time-Zone", defaultValue = "UTC") ZoneId zoneId,
            @RequestParam LocalDate day
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        MealDailyReport mealDailyReport = mealReportService.generateMealDailyReportForDay(userId, day, zoneId);

        return mealDailyReportMapper.toResponseDto(mealDailyReport);
    }

    @GetMapping("/daily/period")
    @Operation(
            summary = "Get daily Meal report for the authenticated User in a specific period",
            description = """
                     Requires User ID in the Authorization header <br>
                     Uses 'X-Time-Zone' header to determine user's time zone (default: UTC)
                    """,
            security = @SecurityRequirement(name = "Authorization")
    )
    public List<MealDailyReportDto> getDailyMealReportForPeriod(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @RequestHeader(value = "X-Time-Zone", defaultValue = "UTC") ZoneId zoneId,
            @RequestParam LocalDate startDay,
            @RequestParam LocalDate endDay
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        List<MealDailyReport> mealDailyReports = mealReportService.generateMealDailyReportsForPeriod(
                userId,
                startDay,
                endDay,
                zoneId
        );

        return mealDailyReportMapper.toResponseDtoList(mealDailyReports);
    }

    @GetMapping("daily/all-tracked")
    @Operation(
            summary = "Get daily Meal reports for days with tracked meals for the authenticated User",
            description = """
                     Requires User ID in the Authorization header <br>
                     Uses 'X-Time-Zone' header to determine user's time zone (default: UTC)
                    """,
            security = @SecurityRequirement(name = "Authorization")
    )
    public List<MealDailyReportDto> getAllMealsForUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @RequestHeader(value = "X-Time-Zone", defaultValue = "UTC") ZoneId zoneId
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        List<MealDailyReport> mealDailyReports = mealReportService.generateAllTrackedMealDailyReports(userId, zoneId);

        return mealDailyReportMapper.toResponseDtoList(mealDailyReports);
    }

}
