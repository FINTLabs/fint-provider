package no.fint.provider.events.trace;

import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FintTraceService {
    @Autowired(required = false)
    private FintTraceRepository fintTraceRepository;

    public void trace(Event event) {
        if (fintTraceRepository != null)
            fintTraceRepository.trace(event);
    }
}
