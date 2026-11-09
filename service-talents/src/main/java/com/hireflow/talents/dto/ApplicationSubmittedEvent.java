package com.hireflow.talents.dto;

import java.time.LocalDate;

/** Recu depuis service-candidatures : publie au depot d'une candidature (routing key application.submitted), consomme par service-talents. */
public record ApplicationSubmittedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        String cvUrl,
        LocalDate date) {
}
