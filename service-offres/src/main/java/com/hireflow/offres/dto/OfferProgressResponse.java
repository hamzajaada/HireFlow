package com.hireflow.offres.dto;

import com.hireflow.offres.enums.OfferStatus;

/** GET /offers/{id}/progress : l'avancement du recrutement. */
public record OfferProgressResponse(Long offerId, OfferStatus statut, Integer nombreEntretiens, Double noteMoyenne) {
}
