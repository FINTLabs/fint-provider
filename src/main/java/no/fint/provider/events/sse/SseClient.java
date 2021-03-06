package no.fint.provider.events.sse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SseClient {
    private String registered;
    private String id;
    private String client;
    private int events;
}
