package no.fint.provider.events.response;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.provider.events.exceptions.UnknownEventException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping(value = "/response", consumes = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @PostMapping
    public void response(@RequestHeader(HeaderConstants.ORG_ID) String orgId,
                         @RequestHeader(name = "x-source", required = false) String source,
                         @RequestHeader(HeaderConstants.CLIENT) String client,
                         @RequestBody Event event) {
        log.debug("Response event received: {}", event);
        log.trace("Event data: {}", event.getData());
        event.setOrgId(orgId);
        if (StringUtils.isNotEmpty(source)) {
            event.setClient(client + " " + source);
        } else {
            event.setClient(client);
        }
        responseService.handleAdapterResponse(event);
    }

    @ExceptionHandler(UnknownEventException.class)
    public ResponseEntity handleUnknownEventException() {
        return ResponseEntity.status(HttpStatus.GONE).body("Unknown Event object from adapter");
    }
}
