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
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Le serveur SMTP et la base sont simulés : on vérifie quels emails partent pour chaque événement. */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notifications;
    @Mock JavaMailSender mailSender;
    @InjectMocks NotificationService service;

    @Test
    void candidatureRecue_envoieUnEmailAuCandidatEtAuRH() {
        service.candidatureRecue(new ApplicationSubmittedEvent(4L, 1L, "nadia@email.com", null, LocalDate.now()));

        ArgumentCaptor<SimpleMailMessage> mails = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(mails.capture());
        List<SimpleMailMessage> envoyes = mails.getAllValues();
        assertArrayEquals(new String[]{"nadia@email.com"}, envoyes.get(0).getTo());
        assertArrayEquals(new String[]{NotificationService.EMAIL_RH}, envoyes.get(1).getTo());
        verify(notifications, times(2)).save(any());
    }

    @Test
    void entretienNote_envoieLaNoteAuRH() {
        service.entretienNote(new InterviewCompletedEvent(2L, 2L, "sara@email.com", 16, "Très bien"));

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertArrayEquals(new String[]{NotificationService.EMAIL_RH}, mail.getValue().getTo());
        assertTrue(mail.getValue().getText().contains("16/20"));
    }

    @Test
    void candidatRetenu_feliciteLeCandidat_etGardeUneTrace() {
        service.candidatRetenu(new ApplicationAcceptedEvent(1L, 1L, "amine@email.com", LocalDate.now()));

        ArgumentCaptor<Notification> trace = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(trace.capture());
        assertEquals("amine@email.com", trace.getValue().getDestinataire());
        assertEquals(TypeNotification.CANDIDAT_RETENU, trace.getValue().getType());
    }

    @Test
    void smtpEnPanne_lExceptionRemonte_etRienNestEnregistre() {
        doThrow(new MailSendException("Mailpit arrêté")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class,
                () -> service.candidatRetenu(new ApplicationAcceptedEvent(1L, 1L, "amine@email.com", LocalDate.now())));
        verify(notifications, never()).save(any());
    }
}
