package com.hireflow.candidatures.realtime;

import com.hireflow.candidatures.common.StatusChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Garde la liste des écrans connectés (Kanban) et leur pousse chaque changement de statut.
 * Personne ne l'appelle directement : Spring appelle onStatusChange à chaque StatusChange publié
 * par ApplicationService. Ainsi Abderahman ne modifie jamais le code de Yasmine.
 */
@Component
@Slf4j
public class StatusEventPublisher {

    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);            // 0 = pas de délai d'expiration
        clients.add(emitter);
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError(e -> clients.remove(emitter));
        return emitter;
    }

    @EventListener
    public void onStatusChange(StatusChange change) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().name("status-changed").data(change));
            } catch (IOException | IllegalStateException e) {
                clients.remove(emitter);                    // écran fermé : on l'oublie
            }
        }
        log.info("Temps réel : candidature {} {} -> {} ({} écran(s))", change.applicationId(),
                change.ancienStatut(), change.nouveauStatut(), clients.size());
    }

    public int connectedClients() {
        return clients.size();
    }
}