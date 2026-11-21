package com.hireflow.notification.dto;

import java.time.LocalDate;

/** Reçu sur application.submitted (publié par service-candidatures). Mêmes champs que le producteur. */
public record ApplicationSubmittedEvent(Long applicationId, Long offerId, String candidatEmail, String cvUrl,
                                        LocalDate date) {
}
