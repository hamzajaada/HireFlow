package com.hireflow.offres.exception;
import com.hireflow.offres.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;
/** Transforme chaque exception en réponse JSON propre (ApiError) avec le bon code HTTP. */
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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e, HttpServletRequest req) {
        FieldError f = e.getBindingResult().getFieldError();
        String msg = f == null ? "Requête invalide" : f.getField() + " : " + f.getDefaultMessage();
        return build(HttpStatus.BAD_REQUEST, msg, req);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)   // ex. ?statut=FERMEE
    ResponseEntity<ApiError> badParam(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Valeur invalide pour " + e.getName(), req);
    }
    private ResponseEntity<ApiError> build(HttpStatus s, String msg, HttpServletRequest req) {
        return ResponseEntity.status(s)
                .body(new ApiError(LocalDateTime.now(), s.value(), s.getReasonPhrase(), msg,
                        req.getRequestURI()));
    }
}