package no.fint.provider.events.sse;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.HeaderConstants;
import no.fint.events.FintEvents;
import no.fint.provider.events.ProviderProps;
import no.fint.provider.events.admin.AdminService;
import no.fint.provider.events.subscriber.DownstreamSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping(value = "/sse", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RestController
public class SseController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SseService sseService;

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private DownstreamSubscriber downstreamSubscriber;

    @Autowired
    private ProviderProps props;

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @RequestHeader("x-allowed-asset-ids") String[] allowedAssetIds,
            @RequestHeader(HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(HeaderConstants.CLIENT) String client,
            @PathVariable String id) {
        log.info("{} should be within {}", orgId, allowedAssetIds);
        log.info("{}: Client {}, ID {}", orgId, client, id);
        if (!Arrays.asList(allowedAssetIds).contains(orgId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).header("x-Error", "Invalid orgID " + orgId).build();
        }
        if (adminService.register(orgId, client)) {
            SseEmitter emitter = sseService.subscribe(id, orgId, client);
            fintEvents.registerDownstreamListener(orgId, downstreamSubscriber);
            return ResponseEntity.ok(emitter);
        } else {
            return ResponseEntity.badRequest().header("x-Error", "Invalid orgID " + orgId).build();
        }
    }

    @GetMapping("/clients")
    public List<SseOrg> getClients() {
        Map<String, FintSseEmitters> clients = sseService.getSseClients();
        List<SseOrg> orgs = new ArrayList<>();
        clients.forEach((key, value) -> {
            List<SseClient> sseClients = new ArrayList<>();
            value.forEach(emitter -> sseClients.add(new SseClient(emitter.getRegistered(), emitter.getId(), emitter.getClient(), emitter.getEventCounter().get())));

            orgs.add(new SseOrg(props.getContextPath(), key, sseClients));
        });
        return orgs;
    }

    @DeleteMapping("/clients")
    public void removeSseClients() {
        sseService.removeAll();
    }

    @GetMapping("/auth-init")
    public void authorize() {
    }

}
