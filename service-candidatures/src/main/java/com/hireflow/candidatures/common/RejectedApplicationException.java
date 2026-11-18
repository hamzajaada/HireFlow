package com.hireflow.candidatures.common;

/** 422 : service-offres a répondu que la candidature n'est pas éligible. */
public class RejectedApplicationException extends RuntimeException {
    public RejectedApplicationException(String message) {
        super(message);
    }
}
