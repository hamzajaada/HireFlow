package com.hireflow.candidatures.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Compétence déclarée par le candidat au dépôt (niveau 1 à 5). */
public record DeclaredSkill(@NotBlank String nom, @NotNull @Min(1) @Max(5) Integer niveau) {
}
