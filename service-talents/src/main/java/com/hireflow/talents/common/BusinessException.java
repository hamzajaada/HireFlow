package com.hireflow.talents.common;

/** 409 : une règle de gestion interdit l'opération (email déjà utilisé...). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
