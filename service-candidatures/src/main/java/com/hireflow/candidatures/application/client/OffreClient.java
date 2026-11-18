package com.hireflow.candidatures.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign ① : name = nom du service dans Eureka. Feign demande l'adresse à Eureka,
 * puis fait un POST HTTP normal vers service-offres.
 */
@FeignClient(name = "service-offres", fallbackFactory = OffreClientFallbackFactory.class)
public interface OffreClient {

    @PostMapping("/offers/{id}/eligibility")
    EligibilityResponse verifier(@PathVariable("id") Long offerId, @RequestBody EligibilityRequest body);
}
