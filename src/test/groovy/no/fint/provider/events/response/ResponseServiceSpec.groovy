package no.fint.provider.events.response

import no.fint.audit.FintAuditService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.events.FintEvents
import no.fint.provider.events.eventstate.EventState
import no.fint.provider.events.eventstate.EventStateService
import no.fint.provider.events.exceptions.UnknownEventException
import org.redisson.api.RBlockingQueue
import spock.lang.Specification

class ResponseServiceSpec extends Specification {
    private ResponseService responseService
    private EventStateService eventStateService
    private FintEvents fintEvents

    void setup() {
        fintEvents = Mock(FintEvents)
        eventStateService = Mock(EventStateService)
        responseService = new ResponseService(fintEvents: fintEvents, eventStateService: eventStateService, fintAuditService: Mock(FintAuditService))
    }

    def "Handle adapter response for health check event"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH.name(), 'test')
        def queue = Mock(RBlockingQueue)

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * fintEvents.getTempQueue(event.getCorrId()) >> queue
        1 * queue.offer(event)
    }

    def "Handle adapter response for event registered in EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * eventStateService.get(event) >> Optional.of(new EventState())
        1 * fintEvents.sendUpstream(event.getOrgId(), event)
        1 * eventStateService.remove(event)
    }

    def "Throw UnknownEventException when event is not found in EventState"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        responseService.handleAdapterResponse(event)

        then:
        1 * eventStateService.get(event) >> Optional.empty()
        thrown(UnknownEventException)
    }
}
