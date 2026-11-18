package com.hireflow.offres.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
/** Corps de POST /offers et PUT /offers/{id}. */
public record JobOfferRequest(
        @NotBlank String intitule,
        String departement,
        @PositiveOrZero BigDecimal salaireMax,
        LocalDate datePublication,
        @Valid List<SkillRequest> requiredSkills) {
}