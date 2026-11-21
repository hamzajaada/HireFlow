package com.hireflow.notification.service;

import com.hireflow.notification.dto.ApplicationAcceptedEvent;
import com.hireflow.notification.dto.ApplicationSubmittedEvent;
import com.hireflow.notification.dto.InterviewCompletedEvent;
import com.hireflow.notification.dto.NotificationEmailDto;
import com.hireflow.notification.entite.Notification;
import com.hireflow.notification.enums.TypeNotification;
import com.hireflow.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transforme chaque événement métier en email, envoyé au serveur SMTP (Mailpit en local),
 * puis garde une trace en base. Si le SMTP échoue, l'exception remonte au listener :
 * Spring AMQP réessaie (3 tentatives, cf. config-repo), puis rejette le message.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    public static final String EMAIL_RH = "rh@hireflow.local";

    private final NotificationRepository notifications;
    private final JavaMailSender mailSender;

    @Value("${hireflow.mail.from:noreply@hireflow.local}")
    private String expediteur = "noreply@hireflow.local";

    public void candidatureRecue(ApplicationSubmittedEvent e) {
        envoyer(TypeNotification.CANDIDATURE_RECUE, e.candidatEmail(), e.applicationId(),
                "Candidature n°" + e.applicationId() + " bien reçue",
                "Bonjour,\n\nVotre candidature n°" + e.applicationId() + " à l'offre " + e.offerId()
                        + " a bien été reçue. Nous reviendrons vers vous rapidement.\n\nL'équipe RH Hire-Flow");
        envoyer(TypeNotification.CANDIDATURE_RECUE, EMAIL_RH, e.applicationId(),
                "Nouvelle candidature n°" + e.applicationId(),
                "Nouvelle candidature n°" + e.applicationId() + " de " + e.candidatEmail()
                        + " sur l'offre " + e.offerId() + ".");
    }

    public void entretienNote(InterviewCompletedEvent e) {
        envoyer(TypeNotification.ENTRETIEN_NOTE, EMAIL_RH, e.applicationId(),
                "Entretien noté : candidature n°" + e.applicationId(),
                "L'entretien de " + e.candidatEmail() + " (candidature n°" + e.applicationId() + ") a été noté "
                        + e.note() + "/20.\nAvis : " + e.avis() + "\n\nUne décision est attendue.");
    }

    public void candidatRetenu(ApplicationAcceptedEvent e) {
        envoyer(TypeNotification.CANDIDAT_RETENU, e.candidatEmail(), e.applicationId(),
                "Félicitations : votre candidature est retenue",
                "Bonjour,\n\nFélicitations ! Votre candidature à l'offre " + e.offerId()
                        + " est retenue. Bienvenue chez nous.\n\nL'équipe RH Hire-Flow");
    }

    /** Test manuel (POST /notifications/email) : envoi direct, sans trace en base. */
    public void envoyerEmail(NotificationEmailDto dto) {
        smtp(dto.destinataire(), dto.objet(), dto.contenu());
    }

    public List<Notification> lister(String destinataire) {
        return destinataire == null
                ? notifications.findAllByOrderByIdDesc()
                : notifications.findByDestinataireIgnoreCaseOrderByIdDesc(destinataire);
    }

    private void envoyer(TypeNotification type, String destinataire, Long applicationId, String objet,
                         String contenu) {
        smtp(destinataire, objet, contenu);                 // d'abord l'email : s'il échoue, rien n'est enregistré
        notifications.save(Notification.builder()
                .type(type)
                .destinataire(destinataire)
                .applicationId(applicationId)
                .message(objet)
                .date(LocalDateTime.now())
                .build());
    }

    private void smtp(String destinataire, String objet, String contenu) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire);
        message.setSubject(objet);
        message.setText(contenu);
        mailSender.send(message);
        log.info("Email envoyé à {} : {}", destinataire, objet);
    }
}
