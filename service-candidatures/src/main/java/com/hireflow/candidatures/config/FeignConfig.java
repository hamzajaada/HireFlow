package com.hireflow.candidatures.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Active les vrais clients Feign, SAUF avec le profil "fake" (sprint 1).
 * Avec "fake", ce sont OffreClientFake et TalentClientFake qui répondent (fichiers JSON).
 */
@Configuration
@Profile("!fake")
@EnableFeignClients(basePackages = "com.hireflow.candidatures")
public class FeignConfig {
}
