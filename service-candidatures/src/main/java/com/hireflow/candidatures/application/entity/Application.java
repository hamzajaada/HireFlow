package com.hireflow.candidatures.application.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

/** Candidature : 1 candidat + 1 offre + 1 CV + 1 statut (membre 3). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lien logique vers JobOffer (service-offres), pas de cle etrangere SQL. */
    @Column(nullable = false)
    private Long offerId;

    /** Lien logique vers Candidate (service-talents). */
    @Column(nullable = false)
    private String candidatEmail;

    private String cvUrl;

    @Column(length = 4000)
    private String lettreMotivation;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus statut = ApplicationStatus.DEPOSEE;
}
