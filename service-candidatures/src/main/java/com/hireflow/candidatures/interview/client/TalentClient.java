package com.hireflow.candidatures.interview.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
/** Feign ② : Feign demande à Eureka l'adresse de service-talents, puis fait un GET HTTP. */
@FeignClient(name = "service-talents", fallbackFactory = TalentClientFallbackFactory.class)
public interface TalentClient {
    @GetMapping("/candidates/by-email/{email}/profile")
    CandidateProfile profil(@PathVariable("email") String email);
}
