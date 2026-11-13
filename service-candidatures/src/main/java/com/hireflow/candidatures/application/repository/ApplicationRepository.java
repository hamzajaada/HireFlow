package com.hireflow.candidatures.application.repository;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOfferId(Long offerId);

    /** RG2 : un candidat ne postule qu'une fois à la même offre. */
    boolean existsByOfferIdAndCandidatEmailIgnoreCase(Long offerId, String candidatEmail);

    /** RG9 : un seul RETENU par offre. */
    boolean existsByOfferIdAndStatut(Long offerId, ApplicationStatus statut);
}
