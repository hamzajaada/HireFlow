package com.hireflow.talents.dto;

import com.hireflow.talents.entite.ScreeningTest;

import java.time.LocalDate;

public record ScreeningTestResponse(Long id, Long candidateId, String test, Integer score, LocalDate date) {

    public static ScreeningTestResponse from(ScreeningTest t) {
        return new ScreeningTestResponse(t.getId(), t.getCandidate().getId(), t.getTest(), t.getScore(), t.getDate());
    }
}
