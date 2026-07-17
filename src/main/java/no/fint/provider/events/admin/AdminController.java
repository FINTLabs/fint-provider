package no.fint.provider.events.admin;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.events.FintEvents;
import no.fint.provider.events.subscriber.DownstreamSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DownstreamSubscriber downstreamSubscriber;

    @GetMapping("/orgIds")
    public List<Map> getOrganizations() {
        return adminService.getOrgIds().entrySet().stream().map(entry -> ImmutableMap.of(
                "orgId", entry.getKey(),
                "registered", new Date(entry.getValue())
        )).collect(Collectors.toList());
    }

    @GetMapping("/orgIds/{orgId:.+}")
    public ResponseEntity getOrganization(@PathVariable String orgId) {
        if (adminService.isRegistered(orgId)) {
            return ResponseEntity.ok(ImmutableMap.of(
                    "orgId", orgId,
                    "registered", new Date(adminService.getTimestamp(orgId))
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/orgIds/{orgId:.+}")
    public ResponseEntity registerOrgId(
            @PathVariable String orgId,
            @RequestHeader(HeaderConstants.CLIENT) String client) {
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
