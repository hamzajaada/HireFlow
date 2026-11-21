package com.hireflow.candidatures.application.dto;

import com.hireflow.candidatures.application.entite.Application;

/** Une carte du Kanban. */
public record ApplicationCard(Long id, Long offerId, String candidatEmail) {

    public static ApplicationCard from(Application a) {
        return new ApplicationCard(a.getId(), a.getOfferId(), a.getCandidatEmail());
    }
}
