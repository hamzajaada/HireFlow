package com.hireflow.offres.exception;
/** 404 : la ressource demandée n'existe pas. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}