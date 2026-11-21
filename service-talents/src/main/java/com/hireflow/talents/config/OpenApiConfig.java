package com.hireflow.talents.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** En-tête de la page Swagger : http://localhost:8083/swagger-ui.html */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI talentsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Hire-Flow · service-talents")
                .version("sprint 2")
                .description("Candidats et tests de compétences (membre 2). "
                        + "Expose le profil d'un candidat à service-candidatures (Feign ②) et consomme "
                        + "3 événements RabbitMQ (exchange hireflow.events) : application.submitted (création "
                        + "du profil, statut EN_PROCESSUS), interview.completed (test « Entretien », EVALUE) et "
                        + "application.accepted (EMBAUCHE + email d'onboarding simulé). Un message en échec "
                        + "après 3 essais est conservé dans la file talents.dlq."));
    }
}
