package com.hireflow.candidatures.application.dto;

import java.time.LocalDate;

/** Publie au depot d'une candidature (routing key application.submitted), consomme par service-talents. */
public record ApplicationSubmittedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        String cvUrl,
        LocalDate date) {
}
