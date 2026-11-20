package com.hireflow.offres.messaging;

import com.hireflow.offres.config.RabbitConfig;
import com.hireflow.offres.dto.ApplicationAcceptedEvent;
import com.hireflow.offres.dto.InterviewCompletedEvent;
import com.hireflow.offres.exception.ResourceNotFoundException;
import com.hireflow.offres.service.JobOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Écoute les 2 événements de service-candidatures. Les files sont déclarées dans RabbitConfig. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OfferEventsListener {

    private final JobOfferService service;

    @RabbitListener(queues = RabbitConfig.QUEUE_INTERVIEW_COMPLETED)
    public void onInterviewCompleted(InterviewCompletedEvent e) {
        log.info("Entretien noté {}/20 pour l'offre {}", e.note(), e.offerId());
        try {
            service.enregistrerEntretien(e.offerId(), e.note());
        } catch (ResourceNotFoundException ex) {
            log.warn("Événement ignoré : {}", ex.getMessage());   // offre supprimée entre-temps
        }
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_APPLICATION_ACCEPTED)
    public void onApplicationAccepted(ApplicationAcceptedEvent e) {
        log.info("Candidature {} retenue : clôture de l'offre {}", e.applicationId(), e.offerId());
        try {
            service.cloturer(e.offerId());
        } catch (ResourceNotFoundException ex) {
            log.warn("Événement ignoré : {}", ex.getMessage());
        }
    }
}
