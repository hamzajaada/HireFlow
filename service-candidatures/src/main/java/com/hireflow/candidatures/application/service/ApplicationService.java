package com.hireflow.candidatures.application.service;

import com.hireflow.candidatures.application.client.EligibilityRequest;
import com.hireflow.candidatures.application.client.EligibilityResponse;
import com.hireflow.candidatures.application.client.OffreClient;
import com.hireflow.candidatures.application.dto.ApplicationRequest;
import com.hireflow.candidatures.application.dto.ApplicationResponse;
import com.hireflow.candidatures.application.dto.DeclaredSkill;
import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.repository.ApplicationRepository;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.common.RejectedApplicationException;
import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.common.StatusChange;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applications;
    private final OffreClient offreClient;                  // le vrai Feign, ou OffreClientFake avec le profil "fake"
    private final ApplicationEventPublisher springEvents;   // fourni par Spring : pour le temps réel d'Abderahman

    /** Dépôt : RG1 à RG5. */
    public ApplicationResponse deposer(ApplicationRequest req) {
        if (applications.existsByOfferIdAndCandidatEmailIgnoreCase(req.offerId(), req.candidatEmail())) {
            throw new BusinessException("Ce candidat a déjà postulé à l'offre " + req.offerId());
        }

        List<DeclaredSkill> competences = req.competences() == null ? List.of() : req.competences();
        EligibilityResponse elig = offreClient.verifier(req.offerId(),
                new EligibilityRequest(req.candidatEmail(), competences));

        if (!elig.ouverte()) {
            throw new RejectedApplicationException(
                    "Candidature refusée : l'offre " + req.offerId() + " n'est pas OUVERTE");
        }
        if (!elig.competencesCouvertes()) {
            String manquantes = elig.competencesManquantes().stream()
                    .map(m -> m.nom() + " (minimum " + m.niveauMinimum() + ")")
                    .collect(Collectors.joining(", "));
            throw new RejectedApplicationException(
                    "Candidature refusée : compétences insuffisantes : " + manquantes);
        }

        Application app = applications.save(Application.builder()
                .offerId(req.offerId())
                .candidatEmail(req.candidatEmail().trim().toLowerCase())
                .cvUrl(req.cvUrl())
                .lettreMotivation(req.lettreMotivation())
                .date(LocalDate.now())
                .build());                                  // statut = DEPOSEE par défaut
        return ApplicationResponse.from(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll(Long offerId, ApplicationStatus statut) {
        return applications.findAll().stream()
                .filter(a -> offerId == null || a.getOfferId().equals(offerId))
                .filter(a -> statut == null || a.getStatut() == statut)
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return ApplicationResponse.from(getEntity(id));
    }

    /** Décision du RH : RG6 à RG9. */
    public ApplicationResponse decider(Long id, ApplicationStatus decision) {
        if (decision != ApplicationStatus.RETENUE && decision != ApplicationStatus.REFUSEE) {
            throw new IllegalArgumentException("seules les décisions RETENUE ou REFUSEE sont acceptées");
        }
        Application app = getEntity(id);
        if (app.getStatut() != ApplicationStatus.EN_ENTRETIEN) {
            throw new BusinessException("Transition " + app.getStatut() + " -> " + decision
                    + " interdite : la candidature doit être EN_ENTRETIEN");
        }
        if (decision == ApplicationStatus.RETENUE
                && applications.existsByOfferIdAndStatut(app.getOfferId(), ApplicationStatus.RETENUE)) {
            throw new BusinessException("Un candidat est déjà retenu pour l'offre " + app.getOfferId());
        }
        ApplicationStatus avant = app.getStatut();
        app.setStatut(decision);
        springEvents.publishEvent(new StatusChange(id, avant, decision, LocalDateTime.now())); // temps réel
        return ApplicationResponse.from(app);
    }

    /** Appelé par Abderahman quand il planifie un entretien (RG10). Écrit ensemble au kick-off. */
    public Application passerEnEntretien(Long id) {
        Application app = getEntity(id);
        ApplicationStatus avant = app.getStatut();
        switch (avant) {
            case DEPOSEE -> app.setStatut(ApplicationStatus.EN_ENTRETIEN);
            case EN_ENTRETIEN -> { }                        // deuxième entretien : rien à changer
            default -> throw new BusinessException("La candidature " + id + " est déjà " + avant);
        }
        if (avant != app.getStatut()) {
            springEvents.publishEvent(new StatusChange(id, avant, app.getStatut(), LocalDateTime.now()));
        }
        return app;
    }

    /** Utilisé aussi par Abderahman. Écrit ensemble au kick-off. */
    public Application getEntity(Long id) {
        return applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature " + id + " introuvable"));
    }
}
