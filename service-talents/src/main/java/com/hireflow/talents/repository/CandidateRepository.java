package com.hireflow.talents.repository;

import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Candidate> findByStatut(CandidateStatus statut);
}
