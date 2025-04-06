package org.nikolait.assignment.caloriex.exception.handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.nikolait.assignment.caloriex.exception.EntityAlreadyExistsException;
import org.nikolait.assignment.caloriex.exception.UnauthorizedException;
import org.nikolait.assignment.caloriex.exception.UnprocessableEntityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ErrorResponse handleBindExceptions(BindException ex) {
        return ErrorResponse.builder(ex, ProblemDetail.forStatus(HttpStatus.BAD_REQUEST))
                .type(URI.create(ex.getClass().getSimpleName()))
                .property("errors", extractFieldErrors(ex))
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    public ErrorResponse handleValidationException(ValidationException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ErrorResponse handleUnprocessableEntity(UnprocessableEntityException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ErrorResponse handleEntityAlreadyExists(EntityAlreadyExistsException ex) {
        return ErrorResponse.builder(ex, HttpStatus.CONFLICT, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ErrorResponse handleMissingHeader(MissingRequestHeaderException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResponse handleUnauthorized(UnauthorizedException ex) {
        return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                .type(URI.create(ex.getClass().getSimpleName()))
                .build();
    }

    private Map<String, String> extractFieldErrors(BindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toUnmodifiableMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : ""
                ));
    }
}
