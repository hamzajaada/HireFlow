package com.hireflow.offres.entity;

import jakarta.persistence.*;
import lombok.*;

/** Competence exigee par une offre. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id")
    private JobOffer offer;

    @Column(nullable = false)
    private String nom;

    private Integer niveauMinimum;
}
