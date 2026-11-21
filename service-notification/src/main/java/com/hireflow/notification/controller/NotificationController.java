package com.hireflow.notification.controller;

import com.hireflow.notification.entite.Notification;
import com.hireflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lecture seule : les notifications sont créées uniquement par les événements RabbitMQ. */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    /** GET /notifications, ou GET /notifications?destinataire=nadia@email.com */
    @GetMapping
    public List<Notification> lister(@RequestParam(required = false) String destinataire) {
        return service.lister(destinataire);
    }
}
