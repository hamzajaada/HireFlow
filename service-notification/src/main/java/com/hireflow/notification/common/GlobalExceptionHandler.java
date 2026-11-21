package com.hireflow.notification.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Ne concerne que les appels HTTP : les erreurs du listener RabbitMQ sont gérées par la politique de reprise. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MailException.class)
    public ResponseEntity<String> erreurSmtp(MailException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Impossible de transmettre l'email au serveur SMTP");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> invalide(MethodArgumentNotValidException ex) {
        var f = ex.getBindingResult().getFieldError();
        return ResponseEntity.badRequest().body(f == null ? "Requête invalide" : f.getField() + " : " + f.getDefaultMessage());
    }
}
