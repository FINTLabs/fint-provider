package no.fint.provider.events.trace;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TraceService {
    @Autowired(required = false)
    private TraceRepository traceRepository;

    public void trace(Event event) {
        if (traceRepository != null)
            traceRepository.trace(event);
    }
}
