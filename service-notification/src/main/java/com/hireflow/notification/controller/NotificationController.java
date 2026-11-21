package com.hireflow.notification.controller;

import com.hireflow.notification.dto.NotificationEmailDto;
import com.hireflow.notification.entite.Notification;
import com.hireflow.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    /** Historique : GET /notifications, ou GET /notifications?destinataire=nadia@email.com */
    @GetMapping
    public List<Notification> lister(@RequestParam(required = false) String destinataire) {
        return service.lister(destinataire);
    }

    /** Test manuel de l'envoi SMTP : 204 si Mailpit a accepté l'email, 400 si invalide, 503 si SMTP injoignable. */
    @PostMapping("/email")
    public ResponseEntity<Void> envoyer(@Valid @RequestBody NotificationEmailDto notification) {
        service.envoyerEmail(notification);
        return ResponseEntity.noContent().build();
    }
}
