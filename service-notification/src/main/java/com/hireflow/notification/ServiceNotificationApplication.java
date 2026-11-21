package com.hireflow.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service de notification découplé (V5) : il n'est appelé par personne.
 * Il écoute les 3 événements RabbitMQ de l'exchange hireflow.events et prévient les personnes concernées.
 */
@SpringBootApplication
public class
ServiceNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceNotificationApplication.class, args);
    }
}
