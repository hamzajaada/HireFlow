package com.hireflow.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corps de POST /notifications/email (test manuel de l'envoi, comme dans le TP). */
public record NotificationEmailDto(@NotBlank @Email String destinataire, @NotBlank String objet,
                                   @NotBlank String contenu) {
}
