package no.fint.provider.events.sse

import no.fint.event.model.Event
import no.fint.provider.events.ProviderProps
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class SseServiceSpec extends Specification {
    private SseService sseService
    private ProviderProps props

    void setup() {
        props = Mock(ProviderProps) {
            getMaxNumberOfEmitters() >> 5
        }
        sseService = new SseService(providerProps: props)
    }

    def "Return SseEmitter when subscribing with new orgId"() {
        when:
        def emitter = sseService.subscribe('123', 'hfk.no', 'client')

        then:
        emitter != null
    }


    def "Return new SseEmitter when subscribing with already registered orgId"() {
        when:
        def emitter1 = sseService.subscribe('123', 'hfk.no', 'client')
        def emitter2 = sseService.subscribe('234', 'hfk.no', 'client')

        then:
        sseService.getSseClients().size() == 1
        emitter1 != emitter2
    }

    def "Send Event to registered emitter"() {
        given:
        def event = new Event('hfk.no', 'FK', 'GET_ALL_EMPLOYEES', 'test')
        sseService.subscribe('123', 'hfk.no', 'client')

        when:
        sseService.send(event)

        then:
        noExceptionThrown()
    }

    def "Do not send event when orgId does not have registered emitters"() {
        given:
        sseService = new SseService(providerProps: props, clients: [:])

        when:
        sseService.send(new Event(orgId: 'hfk.no'))

        then:
        noExceptionThrown()
    }

    def "Handle exception when trying to send message"() {
        given:
        def emitter = Mock(FintSseEmitter) {
            _ * getActions() >> Collections.singleton('GET_MONKEY')
        }
        def emitters = FintSseEmitters.with(5)
        emitters.add(emitter)
        def clients = ['hfk.no': emitters] as ConcurrentHashMap
        sseService = new SseService(providerProps: props, clients: clients)

        when:
        sseService.send(new Event(orgId: 'hfk.no', action: 'GET_MONKEY'))

        then:
        1 * emitter.send(_ as SseEmitter.SseEventBuilder) >> { throw new IOException('Test exception') }
    }

    def "Remove all registered SSE clients"() {
        given:
        def clients = ['rogfk.no': FintSseEmitters.with(5)] as ConcurrentHashMap
        sseService = new SseService(providerProps: props, clients: clients)

        when:
        sseService.removeAll()
        def registeredClients = sseService.getSseClients()

        then:
        registeredClients.size() == 0
    }

    def "Run complete method for all registered emitters when shutting down"() {
        given:
        def emitter = Mock(FintSseEmitter)
        def emitters = FintSseEmitters.with(5)
        emitters.add(emitter)
        def clients = ['hfk.no': emitters] as ConcurrentHashMap
        sseService = new SseService(providerProps: props, clients: clients)

        when:
        sseService.shutdown()

        then:
        1 * emitter.complete()
    }

    def "Run heartbeat"() {
        given:
        def emitter = Mock(FintSseEmitter) {
            getId() >> 'fake'
        }
        def emitters = FintSseEmitters.with(5)
        emitters.add(emitter)
        def clients = ['hfk.no': emitters] as ConcurrentHashMap
        sseService = new SseService(providerProps: props, clients: clients)
        def heartbeat = new Heartbeat(sseService: sseService)

        when:
        heartbeat.sendHeartbeat()

        then:
        1 * emitter.send(_ as SseEmitter.SseEventBuilder) >> { throw new IOException('Test exception') }
    }
}
