package no.fint.provider.events.trace;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Repository
@Slf4j
@ConditionalOnProperty("fint.provider.trace.enabled")
public class FintTraceRepository {
    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient blobContainerClient;
    private final ObjectMapper objectMapper;
    private final Executor executor;
    private final Filter filter;

    public FintTraceRepository(
            @Value("${fint.provider.trace.connection-string}") String connectionString,
            @Value("${fint.provider.trace.container-name}") String containerName,
            ObjectMapper objectMapper,
            Filter filter) {
        blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        this.objectMapper = objectMapper;
        this.filter = filter;
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
        }
        executor = Executors.newWorkStealingPool();
        log.info("Connected to {}", blobServiceClient.getAccountName());
    }

    public void trace(Event event) {
        if (filter.applies(event.getOrgId())) {
            executor.execute(() -> store(event));
        }
    }


    private void store(Event event) {
        if (!filter.contains(event.getOrgId())) {
            return;
        }
        String name = String.format("%s/%s/%tF/%s.json",
                event.getOrgId(),
                event.getStatus(),
                event.getTime(),
                event.getCorrId());
        BlobClient blobClient = blobContainerClient.getBlobClient(name);
        try {
            objectMapper.writeValue(blobClient.getBlockBlobClient().getBlobOutputStream(), event.getData());
        } catch (IOException e) {
            log.debug("Unable to write event {}", event.getCorrId(), e);
        }
        blobClient.setMetadata(ImmutableMap.<String, String>builder()
                .put("timestamp", LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), ZoneId.systemDefault()).toString())
                .put("action", String.valueOf(event.getAction()))
                .put("client", String.valueOf(event.getClient()))
                .put("corrId", String.valueOf(event.getCorrId()))
                .put("message", String.valueOf(event.getMessage()))
                .put("operation", String.valueOf(event.getOperation()))
                .put("orgId", String.valueOf(event.getOrgId()))
                .put("responseStatus", String.valueOf(event.getResponseStatus()))
                .put("source", String.valueOf(event.getSource()))
                .put("status", String.valueOf(event.getStatus()))
                .build());
    }


}
