package com.hireflow.candidatures.application.service;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.repository.ApplicationRepository;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.common.StatusChange;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Version du kick-off (lundi 9) : seulement ce dont Abderahman a besoin. Yasmine complète ensuite. */
@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applications;
    private final ApplicationEventPublisher springEvents;   // fourni par Spring : pour le temps réel d'Abderahman

    /** Appelé par Abderahman quand il planifie un entretien (RG10). */
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

    /** Utilisé aussi par Abderahman. */
    public Application getEntity(Long id) {
        return applications.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature " + id + " introuvable"));
    }
}
