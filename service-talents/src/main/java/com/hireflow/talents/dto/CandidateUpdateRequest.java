package com.hireflow.talents.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** L'email ne se modifie pas : c'est lui qui relie le candidat aux candidatures. */
public record CandidateUpdateRequest(
        @NotBlank String nom,
        String telephone,
        @Min(0) @Max(50) Integer experienceAnnees) {
}
