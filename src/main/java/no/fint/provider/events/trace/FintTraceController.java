package no.fint.provider.events.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/admin/trace", produces = MediaType.APPLICATION_JSON_VALUE)
public class FintTraceController {
    @Autowired
    private Filter filter;

    @PutMapping("{orgId}")
    public void startTrace(
            @PathVariable String orgId
    ) {
        filter.add(orgId);
    }

    @DeleteMapping("{orgId}")
    public void stopTrace(
            @PathVariable String orgId
    ) {
        filter.remove(orgId);
    }

    @GetMapping
    public Set<String> getStatus() {
        return filter.stream().collect(Collectors.toSet());
    }

}
