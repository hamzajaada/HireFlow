package com.hireflow.candidatures.interview.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
/**
 * Le profil est une aide pour l'évaluateur, pas une condition : si service-talents est arrêté
 * ou ne connaît pas encore le candidat, on renvoie null et l'entretien est planifié quand même.
 */
@Component
@Slf4j
public class TalentClientFallbackFactory implements FallbackFactory<TalentClient> {
    @Override
    public TalentClient create(Throwable cause) {
        return email -> {
            log.warn("Profil de {} indisponible ({}), entretien planifié sans profil", email,
                    cause.toString());
            return null;
        };
    }
}

