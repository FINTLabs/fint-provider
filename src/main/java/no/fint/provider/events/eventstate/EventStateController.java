package no.fint.provider.events.eventstate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(value = "/eventStates", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventStateController {

    @Autowired
    private EventStateService eventStateService;

    @GetMapping
    public Collection<EventState> getEventState() {
        return eventStateService.getEventStates();
    }

}
