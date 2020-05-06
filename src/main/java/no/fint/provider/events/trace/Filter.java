package no.fint.provider.events.trace;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Service
public class Filter {
    private final Set<String> organisations = new ConcurrentSkipListSet<>();
    private volatile int filter;
    private final Lock lock = new ReentrantLock();

    public boolean applies(String orgId) {
        final int code = orgId.hashCode();
        return (filter & code) == code;
    }

    public boolean contains(String orgId) {
        return organisations.contains(orgId);
    }

    public boolean add(String orgId) {
        if (organisations.add(orgId)) {
            lock.lock();
            try {
                updateFilter();
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    public boolean remove(String orgId) {
        if (organisations.remove(orgId)) {
            lock.lock();
            try {
                updateFilter();
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    public Stream<String> stream() {
        return organisations.stream();
    }

    private void updateFilter() {
        filter = organisations
                .stream()
                .mapToInt(Objects::hashCode)
                .reduce((a, b) -> a | b)
                .orElse(0);
    }

}
