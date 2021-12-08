package no.fint.provider.events.sse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.Set;

@Data
@AllArgsConstructor
public class SseClient {
    private String registered;
    private String id;
    private String client;
    private int events;
    private Set<String> actions;
}
