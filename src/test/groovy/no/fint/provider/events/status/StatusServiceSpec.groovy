package no.fint.provider.events.status

import no.fint.audit.FintAuditService
import no.fint.event.model.Event
import no.fint.event.model.Status
import no.fint.events.FintEvents
import no.fint.provider.events.eventstate.EventState
import no.fint.provider.events.eventstate.EventStateService
import no.fint.provider.events.exceptions.UnknownEventException
import spock.lang.Specification

class StatusServiceSpec extends Specification {
    private StatusService statusService
    private EventStateService eventStateService
    private FintEvents fintEvents

    void setup() {
        eventStateService = Mock(EventStateService)
        fintEvents = Mock(FintEvents)

        statusService = new StatusService(
                eventStateService: eventStateService,
                fintAuditService: Mock(FintAuditService),
                fintEvents: fintEvents)
    }

    def "Update ttl for event with status PROVIDER_ACCEPTED"() {
        given:
        def event = new Event(orgId: 'rogfk.no', status: Status.PROVIDER_ACCEPTED)
        def eventState = new EventState()
        def originalExpires = eventState.expires

        when:
        statusService.updateEventState(event)

        then:
        1 * eventStateService.get(event) >> Optional.of(eventState)
        eventState.expires > originalExpires
    }

    def "Update ttl for event with status PROVIDER_REJECTED"() {
        given:
        def event = new Event(orgId: 'rogfk.no', status: Status.PROVIDER_REJECTED)

        when:
        statusService.updateEventState(event)

        then:
        1 * eventStateService.get(event) >> Optional.of(new EventState())
        1 * fintEvents.sendUpstream(event.getOrgId(), event)
        1 * eventStateService.remove(event)
    }

    def "Handle non-existing event state"() {
        when:
        statusService.updateEventState(new Event())

        then:
        1 * eventStateService.get(_ as Event) >> Optional.empty()
        thrown(UnknownEventException)
    }
}
