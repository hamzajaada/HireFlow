package com.hireflow.notification;

import com.hireflow.notification.dto.ApplicationAcceptedEvent;
import com.hireflow.notification.dto.ApplicationSubmittedEvent;
import com.hireflow.notification.dto.InterviewCompletedEvent;
import com.hireflow.notification.entite.Notification;
import com.hireflow.notification.enums.TypeNotification;
import com.hireflow.notification.repository.NotificationRepository;
import com.hireflow.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Le repository est simulé : on vérifie quelles notifications sont créées pour chaque événement. */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notifications;
    @InjectMocks NotificationService service;

    @Test
    void candidatureRecue_previentLeCandidatEtLeRH() {
        service.candidatureRecue(new ApplicationSubmittedEvent(4L, 1L, "nadia@email.com", null, LocalDate.now()));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications, times(2)).save(captor.capture());
        List<Notification> envoyees = captor.getAllValues();
        assertEquals("nadia@email.com", envoyees.get(0).getDestinataire());
        assertEquals(NotificationService.EMAIL_RH, envoyees.get(1).getDestinataire());
        assertEquals(TypeNotification.CANDIDATURE_RECUE, envoyees.get(0).getType());
    }

    @Test
    void entretienNote_previentLeRHAvecLaNote() {
        service.entretienNote(new InterviewCompletedEvent(2L, 2L, "sara@email.com", 16, "Très bien"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(captor.capture());
        assertEquals(NotificationService.EMAIL_RH, captor.getValue().getDestinataire());
        assertTrue(captor.getValue().getMessage().contains("16/20"));
    }

    @Test
    void candidatRetenu_feliciteLeCandidat() {
        service.candidatRetenu(new ApplicationAcceptedEvent(1L, 1L, "amine@email.com", LocalDate.now()));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(captor.capture());
        assertEquals("amine@email.com", captor.getValue().getDestinataire());
        assertEquals(TypeNotification.CANDIDAT_RETENU, captor.getValue().getType());
    }
}
