package com.hireflow.candidatures.application.dto;

import java.time.LocalDate;

/** Publie quand une candidature est RETENUE (routing key application.accepted), consomme par service-offres et service-talents. */
public record ApplicationAcceptedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        LocalDate date) {
}
