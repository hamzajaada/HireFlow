package com.hireflow.candidatures.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class StatusStreamController {

    private final StatusEventPublisher publisher;

    /** Le Kanban ouvre cette connexion une fois et la garde ouverte (Server-Sent Events). */
    @GetMapping(path = "/applications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return publisher.subscribe();
    }
}
