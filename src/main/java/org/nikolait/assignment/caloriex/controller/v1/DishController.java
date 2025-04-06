package org.nikolait.assignment.caloriex.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.DishCreationDto;
import org.nikolait.assignment.caloriex.dto.DishResponseDto;
import org.nikolait.assignment.caloriex.mapper.DishMapper;
import org.nikolait.assignment.caloriex.model.Dish;
import org.nikolait.assignment.caloriex.secutiry.AuthorizationService;
import org.nikolait.assignment.caloriex.service.DishService;
import org.nikolait.assignment.caloriex.ulti.UriUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final AuthorizationService authorizationService;
    private final DishService dishService;
    private final DishMapper dishMapper;

    @PostMapping
    @Operation(
            summary = "Create a Dish for the authenticated user",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public ResponseEntity<Void> createDish(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @Valid @RequestBody DishCreationDto dishCreationDto
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        Dish dish = dishService.createDish(userId, dishMapper.toModel(dishCreationDto));
        return ResponseEntity.created(UriUtil.buildResourceUriForId(dish.getId())).build();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a Dish by ID for the authenticated User",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public DishResponseDto getDish(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader,
            @PathVariable Long id
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        Dish dish = dishService.getUserDish(userId, id);
        return dishMapper.toResponseDto(dish);
    }

    @GetMapping
    @Operation(
            summary = "Get all dishes for the authenticated user",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public List<DishResponseDto> getAllUserDishes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        return dishMapper.toResponseDtoList(dishService.getAllByUserId(userId));
    }
}
