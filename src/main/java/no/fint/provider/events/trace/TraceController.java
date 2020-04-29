package no.fint.provider.events.trace;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/admin/trace", produces = MediaType.APPLICATION_JSON_VALUE)
public class TraceController {
    @Autowired(required = false)
    private TraceRepository traceRepository;

    @PutMapping
    public void startTrace() {
        if (traceRepository != null)
            traceRepository.setTracing(true);
    }

    @DeleteMapping
    public void stopTrace() {
        if (traceRepository != null)
            traceRepository.setTracing(false);
    }

    @GetMapping
    public TraceStatus getStatus() {
        if (traceRepository != null)
            return new TraceStatus(traceRepository.isTracing(), traceRepository.getCounter());
        return null;
    }

    @Data
    public static class TraceStatus {
        private final boolean running;
        private final long counter;
    }
}
