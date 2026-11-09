package com.hireflow.talents.dto;

import java.time.LocalDate;

/** Recu depuis service-candidatures : publie quand une candidature est RETENUE (routing key application.accepted), consomme par service-offres et service-talents. */
public record ApplicationAcceptedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        LocalDate date) {
}
