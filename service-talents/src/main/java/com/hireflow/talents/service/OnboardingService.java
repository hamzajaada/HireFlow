package com.hireflow.talents.service;

import com.hireflow.talents.dto.ApplicationAcceptedEvent;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.enums.CandidateStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OnboardingService {

    private final CandidateService candidateService;

    /** RG7 : candidat EMBAUCHE et email de bienvenue (simulé dans le log), une seule fois. */
    public boolean embaucher(ApplicationAcceptedEvent e) {
        Candidate c = candidateService.creerOuMettreAJour(e.candidatEmail());
        if (!candidateService.avancer(c, CandidateStatus.EMBAUCHE)) {
            log.info("{} est déjà EMBAUCHE : pas de deuxième email", c.getEmail());
            return false;
        }
        log.info("[ONBOARDING] Email de bienvenue envoyé à {} (offre {})", c.getEmail(), e.offerId());
        return true;
    }
}
