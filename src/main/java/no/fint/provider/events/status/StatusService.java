package no.fint.provider.events.status;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.events.FintEvents;
import no.fint.provider.events.ProviderProps;
import no.fint.provider.events.eventstate.EventStateService;
import no.fint.provider.events.exceptions.UnknownEventException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StatusService {

    @Autowired
    private EventStateService eventStateService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private ProviderProps providerProps;

    public void updateEventState(Event event) {
        log.trace("Event received: {}", event);
        if (eventStateService.update(event, getTtl(event.getStatus()))) {
            fintAuditService.audit(event);
            if (event.getStatus() != Status.ADAPTER_ACCEPTED) {
                sendResponse(event);
            }
        } else {
            throw new UnknownEventException(event.getCorrId());
        }
    }

    private int getTtl(Status status) {
        if (status == Status.ADAPTER_ACCEPTED) {
            return providerProps.getResponseTtl();
        }
        return 0;
    }

    private void sendResponse(Event event) {
        if (event.getResponseStatus() == null) {
            event.setResponseStatus(ResponseStatus.REJECTED);
        }
        log.debug("{} adapter did not acknowledge the event (status: {}), sending event upstream.", event.getOrgId(), event.getStatus());
        fintEvents.sendUpstream(event);
    }

}
