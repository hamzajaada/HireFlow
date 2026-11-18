package com.hireflow.talents.messaging;

import com.hireflow.talents.config.RabbitConfig;
import com.hireflow.talents.dto.ApplicationSubmittedEvent;
import com.hireflow.talents.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationSubmittedListener {

    private final CandidateService candidateService;

    @RabbitListener(queues = RabbitConfig.QUEUE_APPLICATION_SUBMITTED)
    public void onSubmitted(ApplicationSubmittedEvent e) {
        candidateService.creerOuMettreAJour(e.candidatEmail());        // statut EN_PROCESSUS
        log.info("[RH] Nouvelle candidature #{} sur l'offre {} ({})", e.applicationId(), e.offerId(), e.candidatEmail());
    }
}
