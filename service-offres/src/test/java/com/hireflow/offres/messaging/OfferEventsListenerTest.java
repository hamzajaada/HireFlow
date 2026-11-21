package com.hireflow.offres.messaging;

import com.hireflow.offres.dto.ApplicationAcceptedEvent;
import com.hireflow.offres.dto.InterviewCompletedEvent;
import com.hireflow.offres.exception.ResourceNotFoundException;
import com.hireflow.offres.service.JobOfferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/** Le listener ne fait que passer le message au service. Pas besoin de RabbitMQ pour le tester. */
@ExtendWith(MockitoExtension.class)
class OfferEventsListenerTest {

    @Mock JobOfferService service;
    @InjectMocks OfferEventsListener listener;

    @Test
    void entretienNote_appelleEnregistrerEntretien() {
        listener.onInterviewCompleted(new InterviewCompletedEvent(10L, 1L, "amine@email.com", 16, "Très bon"));
        verify(service).enregistrerEntretien(1L, 16);
    }

    @Test
    void candidatureRetenue_appelleCloturer() {
        listener.onApplicationAccepted(new ApplicationAcceptedEvent(10L, 1L, "amine@email.com", LocalDate.now()));
        verify(service).cloturer(1L);
    }

    @Test
    void offreInconnue_messageIgnoreSansErreur() {
        doThrow(new ResourceNotFoundException("Offre 99 introuvable")).when(service).cloturer(99L);
        assertDoesNotThrow(() ->
                listener.onApplicationAccepted(new ApplicationAcceptedEvent(10L, 99L, "amine@email.com", null)));
    }
}
