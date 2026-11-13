package com.hireflow.candidatures.common;

import java.time.LocalDateTime;

/** Format d'erreur commun aux 3 services. */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
}
