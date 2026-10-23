package com.hireflow.candidatures.application.repository;

import com.hireflow.candidatures.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOfferId(Long offerId);
}
