package com.hireflow.talents.repository;

import com.hireflow.talents.entite.ScreeningTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningTestRepository extends JpaRepository<ScreeningTest, Long> {

    /** Le plus récent d'abord. */
    List<ScreeningTest> findByCandidateIdOrderByDateDescIdDesc(Long candidateId);

    /** Pour ne pas enregistrer deux fois le même test (message RabbitMQ reçu deux fois). */
    boolean existsByCandidateIdAndTest(Long candidateId, String test);
}
