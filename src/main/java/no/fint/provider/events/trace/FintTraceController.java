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
public class FintTraceController {
    @Autowired(required = false)
    private FintTraceRepository fintTraceRepository;

    @PutMapping
    public void startTrace() {
        if (fintTraceRepository != null)
            fintTraceRepository.setTracing(true);
    }

    @DeleteMapping
    public void stopTrace() {
        if (fintTraceRepository != null)
            fintTraceRepository.setTracing(false);
    }

    @GetMapping
    public TraceStatus getStatus() {
        if (fintTraceRepository != null)
            return new TraceStatus(fintTraceRepository.isTracing(), fintTraceRepository.getCounter());
        return null;
    }

    @Data
    public static class TraceStatus {
        private final boolean running;
        private final long counter;
    }
}
