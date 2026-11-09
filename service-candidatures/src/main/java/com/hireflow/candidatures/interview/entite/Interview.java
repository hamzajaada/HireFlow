package com.hireflow.candidatures.interview.entite;

import com.hireflow.candidatures.application.entite.Application;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

/** Entretien lie a une candidature (membre 4). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    private LocalDate date;

    private String evaluateur;

    private Integer note;

    @Column(length = 2000)
    private String avis;
}
