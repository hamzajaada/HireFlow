package com.hireflow.talents.common;

import java.time.LocalDateTime;

/** Même format d'erreur que les autres services : timestamp, status, error, message, path. */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
}
