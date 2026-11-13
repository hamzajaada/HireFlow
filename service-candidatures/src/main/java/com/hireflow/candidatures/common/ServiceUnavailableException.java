package com.hireflow.candidatures.common;

/** 503 : un service appelé par Feign ne répond pas (levée par le fallback). */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
