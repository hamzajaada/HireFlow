package com.hireflow.notification.dto;

import java.time.LocalDate;

/** Reçu sur application.accepted (publié par service-candidatures). Mêmes champs que le producteur. */
public record ApplicationAcceptedEvent(Long applicationId, Long offerId, String candidatEmail, LocalDate date) {
}
