package no.fint.provider.events.response

import no.fint.audit.FintAuditService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.events.FintEvents
import no.fint.provider.events.eventstate.EventState
import no.fint.provider.events.eventstate.EventStateService
import no.fint.provider.events.exceptions.UnknownEventException
import no.fint.provider.events.trace.FintTraceService
import spock.lang.Specification

class ResponseServiceSpec extends Specification {
    private ResponseService responseService
    private EventStateService eventStateService
    private FintEvents fintEvents

    void setup() {
        fintEvents = Mock(FintEvents)
        eventStateService = Mock(EventStateService)
        responseService = new ResponseService(
                fintEvents: fintEvents,
                eventStateService: eventStateService,
                fintAuditService: Mock(FintAuditService),
                fintTraceService: Mock(FintTraceService))
    }

    def "Handle adapter response for health check event"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH.name(), 'test')

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * fintEvents.sendUpstream(event)
    }

    def "Handle adapter response for event registered in EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * eventStateService.remove(event) >> Optional.of(new EventState(event, 10))
        1 * fintEvents.sendUpstream(event)
    }

    def "Throw UnknownEventException when event is not found in EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * eventStateService.remove(event) >> Optional.empty()
        thrown(UnknownEventException)
    }
}
