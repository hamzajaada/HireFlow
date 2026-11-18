package com.hireflow.candidatures.application.dto;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;

import java.time.LocalDate;

public record ApplicationResponse(Long id, Long offerId, String candidatEmail, String cvUrl,
                                  String lettreMotivation, LocalDate date, ApplicationStatus statut) {

    public static ApplicationResponse from(Application a) {
        return new ApplicationResponse(a.getId(), a.getOfferId(), a.getCandidatEmail(), a.getCvUrl(),
                a.getLettreMotivation(), a.getDate(), a.getStatut());
    }
}
