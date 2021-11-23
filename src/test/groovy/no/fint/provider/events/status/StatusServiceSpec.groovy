package no.fint.provider.events.status

import no.fint.audit.FintAuditService
import no.fint.event.model.Event
import no.fint.event.model.Status
import no.fint.events.FintEvents
import no.fint.provider.events.ProviderProps
import no.fint.provider.events.eventstate.EventStateService
import no.fint.provider.events.exceptions.UnknownEventException
import spock.lang.Specification

class StatusServiceSpec extends Specification {
    private StatusService statusService
    private EventStateService eventStateService
    private FintEvents fintEvents
    private ProviderProps props
    private FintAuditService fintAuditService

    void setup() {
        eventStateService = Mock(EventStateService)
        fintEvents = Mock(FintEvents)
        props = Mock(ProviderProps)
        fintAuditService = Mock(FintAuditService)

        statusService = new StatusService(
                eventStateService: eventStateService,
                fintAuditService: fintAuditService,
                fintEvents: fintEvents,
                providerProps: props)
    }

    def "Update ttl for event with status ADAPTER_ACCEPTED"() {
        given:
        def event = new Event(orgId: 'rogfk.no', status: Status.ADAPTER_ACCEPTED)

        when:
        statusService.updateEventState(event)

        then:
        1 * eventStateService.update(event, props.responseTtl) >> true
        1 * fintAuditService.audit(event)
    }

    def "Update ttl for event with status ADAPTER_REJECTED"() {
        given:
        def event = new Event(orgId: 'rogfk.no', status: Status.ADAPTER_REJECTED)

        when:
        statusService.updateEventState(event)

        then:
        1 * eventStateService.update(event, 0) >> true
        1 * fintEvents.sendUpstream(event)
        1 * fintAuditService.audit(event)
    }

    def "Handle non-existing event state"() {
        when:
        statusService.updateEventState(new Event())

        then:
        1 * eventStateService.update(_ as Event, _) >> false
        thrown(UnknownEventException)
    }
}
