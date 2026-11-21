package com.hireflow.notification.entite;

import com.hireflow.notification.enums.TypeNotification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Une notification envoyée (email simulé), gardée en base pour pouvoir la consulter. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    /** À qui elle est envoyée (le candidat, ou le RH). */
    @Column(nullable = false)
    private String destinataire;

    @Column(nullable = false, length = 1000)
    private String message;

    private Long applicationId;

    private LocalDateTime date;
}
