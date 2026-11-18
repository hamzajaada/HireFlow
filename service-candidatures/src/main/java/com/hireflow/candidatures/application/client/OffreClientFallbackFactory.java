package com.hireflow.candidatures.application.client;

import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.common.ServiceUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Appelé par Resilience4j quand l'appel Feign échoue. On regarde la cause :
 * une 404 de service-offres = offre inconnue ; tout le reste = service indisponible.
 */
@Component
@Slf4j
public class OffreClientFallbackFactory implements FallbackFactory<OffreClient> {

    @Override
    public OffreClient create(Throwable cause) {
        return (offerId, body) -> {
            if (estUne404(cause)) {
                throw new ResourceNotFoundException("Offre " + offerId + " introuvable");
            }
            log.warn("service-offres injoignable : {}", cause.toString());
            throw new ServiceUnavailableException("service-offres est indisponible, réessayez plus tard");
        };
    }

    /** La 404 peut arriver emballée dans une autre exception (timeout, exécution asynchrone). */
    static boolean estUne404(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof FeignException.NotFound) {
                return true;
            }
        }
        return false;
    }
}
