package com.hireflow.offres.repository;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    /** Spring écrit la requête tout seul à partir du nom : WHERE statut = ? */
    List<JobOffer> findByStatut(OfferStatus statut);
}