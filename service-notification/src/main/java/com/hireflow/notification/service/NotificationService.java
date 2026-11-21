package com.hireflow.notification.service;

import com.hireflow.notification.dto.ApplicationAcceptedEvent;
import com.hireflow.notification.dto.ApplicationSubmittedEvent;
import com.hireflow.notification.dto.InterviewCompletedEvent;
import com.hireflow.notification.entite.Notification;
import com.hireflow.notification.enums.TypeNotification;
import com.hireflow.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transforme chaque événement en notification. L'envoi d'email est simulé : on l'écrit dans les logs
 * et on l'enregistre en base. Pour envoyer de vrais emails, il suffirait de remplacer la méthode envoyer().
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public static final String EMAIL_RH = "rh@hireflow.local";

    private final NotificationRepository notifications;

    public void candidatureRecue(ApplicationSubmittedEvent e) {
        envoyer(TypeNotification.CANDIDATURE_RECUE, e.candidatEmail(), e.applicationId(),
                "Votre candidature n°" + e.applicationId() + " à l'offre " + e.offerId() + " a bien été reçue.");
        envoyer(TypeNotification.CANDIDATURE_RECUE, EMAIL_RH, e.applicationId(),
                "Nouvelle candidature n°" + e.applicationId() + " de " + e.candidatEmail() + " sur l'offre " + e.offerId() + ".");
    }

    public void entretienNote(InterviewCompletedEvent e) {
        envoyer(TypeNotification.ENTRETIEN_NOTE, EMAIL_RH, e.applicationId(),
                "L'entretien de " + e.candidatEmail() + " (candidature n°" + e.applicationId() + ") a été noté "
                        + e.note() + "/20. Une décision est attendue.");
    }

    public void candidatRetenu(ApplicationAcceptedEvent e) {
        envoyer(TypeNotification.CANDIDAT_RETENU, e.candidatEmail(), e.applicationId(),
                "Félicitations ! Votre candidature à l'offre " + e.offerId() + " est retenue. Bienvenue chez nous.");
    }

    public List<Notification> lister(String destinataire) {
        return destinataire == null
                ? notifications.findAllByOrderByIdDesc()
                : notifications.findByDestinataireIgnoreCaseOrderByIdDesc(destinataire);
    }

    private void envoyer(TypeNotification type, String destinataire, Long applicationId, String message) {
        notifications.save(Notification.builder()
                .type(type)
                .destinataire(destinataire)
                .applicationId(applicationId)
                .message(message)
                .date(LocalDateTime.now())
                .build());
        log.info("[EMAIL SIMULÉ] à {} : {}", destinataire, message);
    }
}
