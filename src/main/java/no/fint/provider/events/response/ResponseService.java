package no.fint.provider.events.response;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.events.FintEvents;
import no.fint.provider.events.eventstate.EventStateService;
import no.fint.provider.events.exceptions.UnknownEventException;
import no.fint.provider.events.trace.FintTraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ResponseService {

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private FintTraceService fintTraceService;

    public void handleAdapterResponse(Event event) {
        log.debug("{}: Response for {} from {} status {} with {} elements.",
                event.getCorrId(), event.getAction(), event.getOrgId(), event.getStatus(),
                Optional.ofNullable(event.getData()).map(List::size).orElse(0));
        if (event.isHealthCheck()) {
            sendHealthCheckResponse(event);
        } else {
            sendResponse(event);
        }
    }

    private void sendHealthCheckResponse(Event event) {
        fintAuditService.audit(event);
        event.setStatus(Status.UPSTREAM_QUEUE);
        fintEvents.sendUpstream(event);
        fintAuditService.audit(event, Status.UPSTREAM_QUEUE);
    }

    private void sendResponse(Event event) {
        if (eventStateService.update(event, 0)) {
            fintAuditService.audit(event, Status.ADAPTER_RESPONSE);
            event.setStatus(Status.UPSTREAM_QUEUE);
            fintEvents.sendUpstream(event);
            fintAuditService.audit(event, Status.UPSTREAM_QUEUE);
            fintTraceService.trace(event);
        } else {
            fintAuditService.audit(event, Status.ADAPTER_RESPONSE_ORPHANED);
            throw new UnknownEventException(event.getCorrId());
        }
    }
}
