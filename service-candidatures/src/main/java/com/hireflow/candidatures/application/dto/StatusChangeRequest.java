package com.hireflow.candidatures.application.dto;

import com.hireflow.candidatures.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

/** Corps de PATCH /applications/{id}/status : {"statut":"RETENUE"} ou {"statut":"REFUSEE"}. */
public record StatusChangeRequest(@NotNull ApplicationStatus statut) {
}
