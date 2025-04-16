package org.nikolait.assignment.caloriex.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.MealCreationDto;
import org.nikolait.assignment.caloriex.dto.MealResponseDto;
import org.nikolait.assignment.caloriex.mapper.MealMapper;
import org.nikolait.assignment.caloriex.model.Meal;
import org.nikolait.assignment.caloriex.secutiry.AuthorizationService;
import org.nikolait.assignment.caloriex.service.MealService;
import org.nikolait.assignment.caloriex.ulti.UriUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealController {

    private final AuthorizationService authorizationService;
    private final MealService mealService;
    private final MealMapper mealMapper;

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

}
