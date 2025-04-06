package org.nikolait.assignment.caloriex.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.dto.UserCreationDto;
import org.nikolait.assignment.caloriex.dto.UserResponseDto;
import org.nikolait.assignment.caloriex.mapper.UserMapper;
import org.nikolait.assignment.caloriex.model.User;
import org.nikolait.assignment.caloriex.secutiry.AuthorizationService;
import org.nikolait.assignment.caloriex.service.UserService;
import org.nikolait.assignment.caloriex.ulti.UriUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthorizationService authorizationService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate User by email (simplified auth)",
            description = "Returns user ID in the Authorization header"
    )
    public ResponseEntity<Void> authenticateByEmail(@RequestParam String email) {
        Long userId = authorizationService.authenticateByEmail(email);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, String.valueOf(userId))
                .build();
    }

    @PostMapping
    @Operation(
            description = "Returns user ID in the Authorization header"
    )
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreationDto userCreationDto) {
        User user = userMapper.toModel(userCreationDto);
        userService.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, String.valueOf(user.getId()))
                .location(UriUtil.buildResourceUriForPath("me"))
                .build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated User",
            description = "Requires User ID in the Authorization header",
            security = @SecurityRequirement(name = "Authorization")
    )
    public UserResponseDto getAuthenticatedUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authHeader
    ) {
        Long userId = authorizationService.authorizeByHeader(authHeader);
        User user = userService.getUserById(userId);
        return userMapper.toResponseDto(user);
    }

}
