package com.hireflow.candidatures.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Corps de POST /applications. */
public record ApplicationRequest(
        @NotNull Long offerId,
        @NotBlank @Email String candidatEmail,
        String cvUrl,
        @Size(max = 4000) String lettreMotivation,
        @Valid List<DeclaredSkill> competences) {
}
