package no.fint.provider.events.subscriber

import no.fint.audit.FintAuditService
import no.fint.event.model.DefaultActions
import no.fint.event.model.Event
import no.fint.provider.events.ProviderProps
import no.fint.provider.events.eventstate.EventStateService
import no.fint.provider.events.sse.SseService
import no.fint.provider.events.trace.FintTraceService
import spock.lang.Specification

class DownstreamSubscriberSpec extends Specification {
    private DownstreamSubscriber downstreamSubscriber
    private SseService sseService
    private EventStateService eventStateService

    void setup() {
        sseService = Mock(SseService)
        eventStateService = Mock(EventStateService)
        def props = Mock(ProviderProps) {
            getStatusTtl() >> 2
        }

        downstreamSubscriber = new DownstreamSubscriber(
                sseService: sseService,
                eventStateService: eventStateService,
                fintAuditService: Mock(FintAuditService),
                fintTraceService: Mock(FintTraceService),
                providerProps: props
        )
    }

    def "Receive health check"() {
        given:
        def event = new Event('rogfk.no', 'test', DefaultActions.HEALTH.name(), 'test')

        when:
        downstreamSubscriber.accept(event)

        then:
        1 * sseService.send(event)
        event.data.size() == 1
    }

    def "Receive event"() {
        given:
        def event = new Event('rogfk.no', 'test', 'GET_ALL', 'test')

        when:
        downstreamSubscriber.accept(event)

        then:
        1 * sseService.send(event)
        1 * eventStateService.add(event, 2)
        event.data.size() == 0
    }
}
