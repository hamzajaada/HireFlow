package com.hireflow.talents.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

/** Test de competences passe par un candidat. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Column(nullable = false)
    private String test;

    private Integer score;

    private LocalDate date;
}
