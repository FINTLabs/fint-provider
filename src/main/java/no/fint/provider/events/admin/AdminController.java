package no.fint.provider.events.admin;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.plugin.mongo.MongoAuditEvent;
import no.fint.event.model.HeaderConstants;
import no.fint.events.FintEvents;
import no.fint.provider.events.Constants;
import no.fint.provider.events.subscriber.DownstreamSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.metrics.annotation.Timed;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Api(tags = {"Admin"}, description = Constants.SWAGGER_DESC_INTERNAL_API)
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MongoOperations mongo;

    @Autowired
    private DownstreamSubscriber downstreamSubscriber;

    @Timed
    @GetMapping("/audit/events")
    public List<MongoAuditEvent> getAllEvents() {
        return mongo.findAll(MongoAuditEvent.class);
    }

    @Timed
    @GetMapping("/audit/events/since/{when}")
    public List<MongoAuditEvent> queryEventsSince(@PathVariable String when) {
        Duration duration = Duration.parse(when);
        Criteria c = Criteria.where("timestamp").gt(ZonedDateTime.now().minus(duration).toInstant().toEpochMilli());
        Query q = new Query(c);
        return mongo.find(q, MongoAuditEvent.class);
    }

    @Timed
    @GetMapping("/orgIds")
    public List<Map> getOrganizations() {
        return adminService.getOrgIds().entrySet().stream().map(entry -> ImmutableMap.of(
                "orgId", entry.getKey(),
                "registered", new Date(entry.getValue())
        )).collect(Collectors.toList());
    }

    @Timed
    @GetMapping("/orgIds/{orgId:.+}")
    public ResponseEntity getOrganization(@ApiParam(Constants.SWAGGER_X_ORG_ID) @PathVariable String orgId) {
        if (adminService.isRegistered(orgId)) {
            return ResponseEntity.ok(ImmutableMap.of(
                    "orgId", orgId,
                    "registered", new Date(adminService.getTimestamp(orgId))
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Timed
    @PostMapping("/orgIds/{orgId:.+}")
    public ResponseEntity registerOrgId(
            @ApiParam(Constants.SWAGGER_X_ORG_ID) @PathVariable String orgId,
            @ApiParam("ID of client.") @RequestHeader(HeaderConstants.CLIENT) String client) {
        if (adminService.isRegistered(orgId)) {
            return ResponseEntity.noContent().build();
        } else if (adminService.register(orgId, client)) {
            fintEvents.registerDownstreamListener(orgId, downstreamSubscriber);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized orgId " + orgId);
        }
    }
}
