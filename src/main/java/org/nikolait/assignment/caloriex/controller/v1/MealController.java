package org.nikolait.assignment.caloriex.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.MealCreationDto;
import org.nikolait.assignment.caloriex.dto.MealDailyReportDto;
import org.nikolait.assignment.caloriex.dto.MealResponseDto;
import org.nikolait.assignment.caloriex.mapper.MealMapper;
import org.nikolait.assignment.caloriex.model.Meal;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.secutiry.AuthorizationService;
import org.nikolait.assignment.caloriex.service.MealService;
import org.nikolait.assignment.caloriex.service.UserService;
import org.nikolait.assignment.caloriex.ulti.UriUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealController {

    private final AuthorizationService authorizationService;
    private final MealService mealService;
    private final MealMapper mealMapper;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Create a Meal for the authenticated User",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public ResponseEntity<Void> createMeal(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @Valid @RequestBody MealCreationDto mealCreationDto
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        Meal meal = mealService.createMeal(userId, mealMapper.toModel(mealCreationDto));
        return ResponseEntity.created(UriUtil.buildResourceUriForId(meal.getId())).build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a Meal by ID for the authenticated User",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public MealResponseDto getMealById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @PathVariable Long id
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        return mealMapper.toResponseDto(mealService.getUserMeal(userId, id));
    }

    @GetMapping
    @Operation(
            summary = "Get daily meal report for the authenticated User",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public List<MealDailyReportDto> getAllMealsForUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        User user = userService.getUserById(userId);

        List<Meal> meals = mealService.getAllUserMeals(userId);

        return getMealDailyReportDtoList(user, meals);
    }

    @GetMapping("/report/daily/period")
    @Operation(
            summary = "Get daily meal report for the authenticated User in a specific period",
            description = "Returns meals grouped by day with total calories per day for a given period",
            security = @SecurityRequirement(name = "Authorization")
    )
    public List<MealDailyReportDto> getDailyMealReportForPeriod(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @RequestParam LocalDate startDay,
            @RequestParam LocalDate endDay
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        List<Meal> meals = mealService.getUserMealsBetween(userId, startDay, endDay);

        User user = userService.getUserById(userId);

        return getMealDailyReportDtoList(user, meals);
    }

    @GetMapping("/report/daily/today")
    @Operation(
            summary = "Get daily meal report for the authenticated User for today",
            description = "Returns meals for today with total calories and whether the daily calorie target was exceeded",
            security = @SecurityRequirement(name = "Authorization")
    )
    public MealDailyReportDto getDailyMealReportForToday(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        List<Meal> meals = mealService.getUserMealsForToday(userId);

        User user = userService.getUserById(userId);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return getMealDailyReportDto(user, today, meals);
    }

    @GetMapping("/report/daily/{day}")
    @Operation(
            summary = "Get daily meal report for the authenticated User for a specific date",
            description = "Returns meals for the given date with total calories and whether the daily calorie target was exceeded. Format: yyyy-MM-dd",
            security = @SecurityRequirement(name = "Authorization")
    )
    public MealDailyReportDto getDailyMealReportForDate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);

        List<Meal> meals = mealService.getUserMealsForDay(userId, day);

        User user = userService.getUserById(userId);

        return getMealDailyReportDto(user, day, meals);
    }

    private List<MealDailyReportDto> getMealDailyReportDtoList(User user, List<Meal> meals) {
        Map<LocalDate, List<Meal>> grouped = meals.stream()
                .collect(Collectors.groupingBy(
                        meal -> meal.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Meal> dailyMeals = entry.getValue();
                    return getMealDailyReportDto(user, date, dailyMeals);
                })
                .sorted(Comparator.comparing(MealDailyReportDto::date).reversed())
                .toList();
    }

    private MealDailyReportDto getMealDailyReportDto(User user, LocalDate date, List<Meal> dailyMeals) {
        List<MealResponseDto> mealDtoList = mealMapper.toResponseDtoList(dailyMeals);
        int totalCalories = dailyMeals.stream().mapToInt(Meal::getCalories).sum();
        int dailyCalorieTarget = user.getDailyCalorieTarget();
        boolean exceeded = totalCalories > dailyCalorieTarget;
        return new MealDailyReportDto(date, mealDtoList, totalCalories, dailyCalorieTarget, exceeded);
    }

}
