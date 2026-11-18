package com.hireflow.talents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CandidateRequest(
        @NotBlank String nom,
        @NotBlank @Email String email,
        String telephone,
        @Min(0) @Max(50) Integer experienceAnnees) {
}
