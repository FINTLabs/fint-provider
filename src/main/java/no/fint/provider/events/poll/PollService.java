package no.fint.provider.events.poll;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.events.FintEvents;
import no.fint.provider.eventstate.EventStateService;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
public class PollService {

    @Autowired
    private FintEvents events;

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private FintAuditService fintAuditService;

    public Optional<Event> readEvent(String orgId) {
        Optional<Message> message = events.readDownstreamMessage(orgId);
        if (message.isPresent()) {
            byte[] body = message.get().getBody();
            try {
                Event event = objectMapper.readValue(body, Event.class);
                event.setStatus(Status.DELIVERED_TO_PROVIDER);
                eventStateService.addEventState(event);
                fintAuditService.audit(event, true);
                return Optional.of(event);
            } catch (IOException e) {
                log.error("Unable to read Event object", e);
            }
        }

        return Optional.empty();
    }
}
