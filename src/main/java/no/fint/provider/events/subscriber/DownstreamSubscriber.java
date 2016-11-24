package no.fint.provider.events.subscriber;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.provider.events.sse.SseService;
import no.fint.provider.eventstate.EventStateService;
import no.fint.provider.exceptions.EventNotProviderApprovedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DownstreamSubscriber {

    @Autowired
    private SseService sseService;

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private FintAuditService fintAuditService;

    public void receive(Event event) {
        if (eventStateService.exists(event)) {
            resendEvent(event);
        } else {
            event.setStatus(Status.DELIVERED_TO_PROVIDER);
            sseService.send(event);
            eventStateService.addEventState(event);
            fintAuditService.audit(event, true);
            throw new EventNotProviderApprovedException();
        }
    }

    private void resendEvent(Event event) {
        Event persistedEvent = eventStateService.getEvent(event);
        if (eventStateService.exists(persistedEvent, Status.DELIVERED_TO_PROVIDER)) {
            sseService.send(event);
            throw new EventNotProviderApprovedException();
        } else {
            log.info("Event with corrId:{} approved by adapter. Consuming message from queue", event.getCorrId());
        }
    }

}