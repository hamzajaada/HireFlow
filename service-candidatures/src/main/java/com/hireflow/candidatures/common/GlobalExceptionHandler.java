package com.hireflow.candidatures.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/** Partagé par Yasmine (application/) et Abderahman (interview/) : un seul format d'erreur. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> conflict(BusinessException e, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(RejectedApplicationException.class)
    ResponseEntity<ApiError> rejected(RejectedApplicationException e, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), req);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    ResponseEntity<ApiError> unavailable(ServiceUnavailableException e, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e, HttpServletRequest req) {
        FieldError f = e.getBindingResult().getFieldError();
        String msg = f == null ? "Requête invalide" : f.getField() + " : " + f.getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class})
    ResponseEntity<ApiError> badRequest(Exception e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Requête invalide : " + e.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus s, String msg, HttpServletRequest req) {
        return ResponseEntity.status(s)
                .body(new ApiError(LocalDateTime.now(), s.value(), s.getReasonPhrase(), msg, req.getRequestURI()));
    }
}
