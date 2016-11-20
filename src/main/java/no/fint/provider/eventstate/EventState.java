package no.fint.provider.eventstate;

import no.fint.event.model.Event;

import java.io.Serializable;

public class EventState implements Serializable {
    private long timestamp;
    private Event event;

    public EventState() {
        timestamp = System.currentTimeMillis();
    }

    public EventState(Event e) {
        timestamp = System.currentTimeMillis();
        event = e;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}