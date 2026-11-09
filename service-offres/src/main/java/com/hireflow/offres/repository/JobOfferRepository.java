package com.hireflow.offres.repository;

import com.hireflow.offres.entite.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
}
