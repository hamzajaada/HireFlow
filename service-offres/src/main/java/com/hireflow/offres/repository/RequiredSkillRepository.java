package com.hireflow.offres.repository;

import com.hireflow.offres.entity.RequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequiredSkillRepository extends JpaRepository<RequiredSkill, Long> {

    List<RequiredSkill> findByOfferId(Long offerId);
}
