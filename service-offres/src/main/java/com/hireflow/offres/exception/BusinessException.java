package com.hireflow.offres.exception;
/** 409 : une règle de gestion interdit l'opération. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}