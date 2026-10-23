package com.hireflow.events;

import java.time.LocalDate;

/** Publie quand une candidature est RETENUE, consomme par service-offres et service-talents. */
public record ApplicationAcceptedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        LocalDate date) {
}
