package no.fint.provider.eventstate

import no.fint.event.model.Event
import spock.lang.Specification


class EventStateSpec extends Specification {

    def "Create EventState Object"() {
        given:
        Event event = new Event("rogfk.no", "FK", "GET", "client")
        
        when:
        EventState eventState = new EventState(event)

        then:
        eventState.timestamp > 0
        eventState.event.orgId == "rogfk.no"
    }
}