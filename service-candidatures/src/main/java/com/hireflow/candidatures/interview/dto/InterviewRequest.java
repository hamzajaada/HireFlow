package com.hireflow.candidatures.interview.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
/** Corps de POST /applications/{id}/interviews. Pas d'entretien dans le passé. */
public record InterviewRequest(@NotNull @FutureOrPresent LocalDate date, @NotBlank String evaluateur) {
}
