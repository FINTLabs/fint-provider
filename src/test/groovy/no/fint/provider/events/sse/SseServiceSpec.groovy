package no.fint.provider.events.sse

import no.fint.event.model.Event
import spock.lang.Specification

class SseServiceSpec extends Specification {
    private SseService sseService

    void setup() {
        sseService = new SseService()
    }

    def "Return SseEmitter when subscribing with new orgId"() {
        when:
        def emitter = sseService.subscribe("hfk.no")

        then:
        emitter != null
        sseService.getSseEmitter("hfk.no").isPresent()
    }

    def "Send Event to registered emitter"() {
        given:
        def event = new Event("hfk.no", "FK", "GET_ALL_EMPLOYEES", "test")
        sseService.subscribe("hfk.no")

        when:
        sseService.send(event)

        then:
        noExceptionThrown()
    }
}
