package no.fint.provider.events.status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.provider.events.Constants;
import no.fint.provider.events.exceptions.UnknownEventException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = {"status"}, description = "Endpoint where the adapter should report that the event is received and understood.")
@RestController
@RequestMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

    @Autowired
    private StatusService statusService;

    @ApiOperation(value = "", notes = "Post back the event with the status flag telling if the response can be handled or not.")
    @PostMapping
    public void status(@ApiParam(Constants.SWAGGER_X_ORG_ID) @RequestHeader(HeaderConstants.ORG_ID) String orgId,
                       @RequestHeader(name = "x-source", required = false) String source,
                       @RequestHeader(HeaderConstants.CLIENT) String client,
                       @ApiParam(Constants.SWAGGER_EVENT) @RequestBody Event event) {
        log.debug("Status event received: {}", event);
        log.trace("Event data: {}", event.getData());
        event.setOrgId(orgId);
        if (StringUtils.isNotEmpty(source)) {
            event.setClient(client + " " + source);
        } else {
            event.setClient(client);
        }
        statusService.updateEventState(event);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity handleHttpMessageConversionException() {
        return ResponseEntity.badRequest().body("Invalid Event object from adapter");
    }

    @ExceptionHandler(UnknownEventException.class)
    public ResponseEntity handleUnknownEventException() {
        return ResponseEntity.status(HttpStatus.GONE).body("Unknown Event object from adapter");
    }
}
