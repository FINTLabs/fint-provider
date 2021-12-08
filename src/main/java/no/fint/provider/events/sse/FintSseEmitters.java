package no.fint.provider.events.sse;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FintSseEmitters implements Iterable<FintSseEmitter> {
    private final int maxSize;
    private final ConcurrentLinkedDeque<FintSseEmitter> emitters;
    private final Consumer<FintSseEmitter> removeCallback;

    public FintSseEmitters(int maxSize, Consumer<FintSseEmitter> removeCallback) {
        this.maxSize = maxSize;
        this.emitters = new ConcurrentLinkedDeque<>();
        if (removeCallback == null) {
            this.removeCallback = (o) -> {};
        } else {
            this.removeCallback = removeCallback;
        }
    }

    public void add(FintSseEmitter emitter) {
        while (emitters.size() >= maxSize) {
            FintSseEmitter victim = emitters.removeLast();
            removeCallback.accept(victim);
        }

        emitters.addFirst(emitter);
    }

    public void remove(FintSseEmitter emitter) {
        emitters.remove(emitter);
    }

    public Optional<FintSseEmitter> get(String id) {
        return emitters.stream().filter(registered -> registered.getId().equals(id)).findFirst();
    }

    public int size() {
        return emitters.size();
    }

    public Stream<FintSseEmitter> stream() {
        return emitters.stream();
    }

    @Override
    public Iterator<FintSseEmitter> iterator() {
        return new ArrayDeque<>(emitters).iterator();
    }

    public static FintSseEmitters with(int maxSize) {
        return new FintSseEmitters(maxSize, null);
    }

    public static FintSseEmitters with(int maxSize, Consumer<FintSseEmitter> removeCallback) {
        return new FintSseEmitters(maxSize, removeCallback);
    }
}
