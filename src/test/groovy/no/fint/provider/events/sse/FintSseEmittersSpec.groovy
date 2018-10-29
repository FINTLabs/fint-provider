package no.fint.provider.events.sse

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import spock.lang.Specification

import java.util.function.Consumer

class FintSseEmittersSpec extends Specification {
    private static final int MAX_SIZE = 2
    private FintSseEmitters sseEmitters
    private Consumer<SseEmitter> removeCallback
    private boolean callback = false

    void setup() {
        removeCallback = new Consumer<FintSseEmitter>() {
            @Override
            void accept(FintSseEmitter emitter) {
                callback = true
            }
        }
        sseEmitters = new FintSseEmitters(MAX_SIZE, removeCallback)
    }

    def "Add emitter and size is 1"() {
        when:
        sseEmitters.add(new FintSseEmitter())
        def size = sseEmitters.size()

        then:
        size == 1
    }

    def "Add more than max size, size is still max size"() {
        given:
        def first = new FintSseEmitter()
        def last = new FintSseEmitter()

        when:
        sseEmitters.add(first)
        sseEmitters.add(new FintSseEmitter())
        sseEmitters.add(new FintSseEmitter())
        sseEmitters.add(new FintSseEmitter())
        sseEmitters.add(last)
        def size = sseEmitters.size()

        then:
        size == MAX_SIZE
        !sseEmitters.contains(first)
        sseEmitters.contains(last)
        callback
    }

    def "Remove emitter"() {
        given:
        def emitter = new FintSseEmitter()

        when:
        sseEmitters.add(emitter)
        sseEmitters.remove(emitter)
        def size = sseEmitters.size()

        then:
        size == 0
    }
}
