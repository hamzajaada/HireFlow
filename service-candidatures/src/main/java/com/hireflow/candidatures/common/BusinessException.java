package com.hireflow.candidatures.common;

/** 409 : une règle de gestion interdit l'opération (transition de statut, doublon...). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
