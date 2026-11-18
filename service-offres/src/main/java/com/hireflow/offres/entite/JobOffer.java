package com.hireflow.offres.entite;
import com.hireflow.offres.enums.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;
/** Offre d'emploi publiee par le RH. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String intitule;
    private String departement;
    private BigDecimal salaireMax;
    private LocalDate datePublication;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfferStatus statut = OfferStatus.OUVERTE;
    /** Nombre d'entretiens notés pour cette offre (mis à jour par RabbitMQ). */
    @Builder.Default
    private Integer nombreEntretiens = 0;
    /** Moyenne des notes d'entretien sur 20 (null tant qu'aucun entretien n'est noté). */
    private Double noteMoyenne;
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequiredSkill> requiredSkills = new ArrayList<>();
}
