package com.hireflow.events;

import java.time.LocalDate;

/** Publie par service-candidatures au depot, consomme par service-talents. */
public record ApplicationSubmittedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        String cvUrl,
        LocalDate date) {
}
