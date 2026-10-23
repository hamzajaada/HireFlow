package com.hireflow.talents.repository;

import com.hireflow.talents.entity.ScreeningTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScreeningTestRepository extends JpaRepository<ScreeningTest, Long> {

    List<ScreeningTest> findByCandidateIdOrderByDateDesc(Long candidateId);
}
