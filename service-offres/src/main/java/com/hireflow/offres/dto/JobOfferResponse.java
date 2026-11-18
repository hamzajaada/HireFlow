package com.hireflow.offres.dto;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.enums.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
/** Ce que l'API renvoie : jamais l'entité JPA directement. */
public record JobOfferResponse(Long id, String intitule, String departement, BigDecimal salaireMax,
                               LocalDate datePublication, OfferStatus statut, Integer nombreEntretiens,
                               Double noteMoyenne, List<SkillResponse> requiredSkills) {
    public static JobOfferResponse from(JobOffer o) {
        return new JobOfferResponse(o.getId(), o.getIntitule(), o.getDepartement(), o.getSalaireMax(),
                o.getDatePublication(), o.getStatut(), o.getNombreEntretiens(), o.getNoteMoyenne(),
                o.getRequiredSkills().stream().map(SkillResponse::from).toList());
    }
}