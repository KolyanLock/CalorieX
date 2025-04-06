package org.nikolait.assignment.caloriex.secutiry;

import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.exception.UnauthorizedException;
import org.nikolait.assignment.caloriex.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;

    public Long authenticateByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new UnauthorizedException("Email is missing");
        }

        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User with the provided email not found"));
    }

    public Long authorizeByHeader(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new UnauthorizedException("Authorization header is missing");
        }
        try {
            Long userId = parseUserId(authHeader);
            if (!userRepository.existsById(userId)) {
                throw new UnauthorizedException("User with the provided ID not found");
            }
            return userId;
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid Authorization header");
        }
    }

    private Long parseUserId(String authorizationHeader) {
        try {
            return Long.valueOf(authorizationHeader);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid Authorization header");
        }
    }

}
