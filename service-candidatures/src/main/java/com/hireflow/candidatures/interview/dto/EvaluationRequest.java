package com.hireflow.candidatures.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
/** Note sur 20, avis libre. */
public record EvaluationRequest(@NotNull @Min(0) @Max(20) Integer note, @Size(max = 2000) String avis) {
}