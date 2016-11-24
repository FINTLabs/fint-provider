package no.fint.provider.events.poll

import no.fint.audit.FintAuditService
import no.fint.event.model.Event
import no.fint.events.FintEvents
import no.fint.provider.eventstate.EventStateService
import spock.lang.Specification

class PollServiceSpec extends Specification {
    private PollService pollService
    private FintEvents fintEvents
    private EventStateService eventStateService
    private FintAuditService fintAuditService

    void setup() {
        fintEvents = Mock(FintEvents)
        eventStateService = Mock(EventStateService)
        fintAuditService = Mock(FintAuditService)
        pollService = new PollService(events: fintEvents, eventStateService: eventStateService, fintAuditService: fintAuditService)
    }

    def "Return empty no message is on the queue"() {
        when:
        def event = pollService.readEvent('hfk.no')

        then:
        1 * fintEvents.readDownstreamObject('hfk.no', Event) >> Optional.empty()
        !event.isPresent()
    }

    def "Return Event object if message is on queue"() {
        when:
        def event = pollService.readEvent('hfk.no')

        then:
        1 * fintEvents.readDownstreamObject('hfk.no', Event) >> Optional.of(new Event('hfk.no', 'test', 'test', 'test'))
        1 * eventStateService.addEventState(_ as Event)
        1 * fintAuditService.audit(_ as Event, _ as Boolean)
        event.isPresent()
    }
}