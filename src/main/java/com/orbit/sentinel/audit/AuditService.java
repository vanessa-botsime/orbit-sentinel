package com.orbit.sentinel.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.sentinel.events.SecurityEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Appends an event to the hash chain. Returns empty if the event was already recorded
     * (Kafka can redeliver). The single Kafka consumer thread keeps the chain strictly ordered.
     */
    @Transactional
    public Optional<AuditEvent> record(SecurityEvent event) {
        if (repository.existsByEventId(event.eventId())) {
            return Optional.empty();
        }
        String prevHash = repository.findTopByOrderByIdDesc()
        .map(last -> last.getHash())
        .orElse(AuditHasher.GENESIS);

        String eventId = limit(event.eventId(), 64);
        String type = limit(event.type(), 60);
        String actor = limit(event.actor(), 255);
        String source = limit(event.source() == null ? "unknown" : event.source(), 60);
        Instant occurredAt = parseTime(event.timestamp()).truncatedTo(ChronoUnit.MILLIS);
        Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String details = toJson(event.details());

        String hash = AuditHasher.hash(prevHash, eventId, type, actor, source, occurredAt, details);
        return Optional.of(repository.save(
                new AuditEvent(eventId, type, actor, source, occurredAt, receivedAt, details, prevHash, hash)));
    }

    /** Recomputes the whole chain and reports the first record that does not match. */
    public VerificationResult verifyChain() {
        String expectedPrev = AuditHasher.GENESIS;
        long lastId = 0;
        long checked = 0;
        while (true) {
            List<AuditEvent> batch = repository.findByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, 500));
            if (batch.isEmpty()) {
                break;
            }
            for (AuditEvent e : batch) {
                String recomputed = AuditHasher.hash(e.getPrevHash(), e.getEventId(), e.getType(),
                        e.getActor(), e.getSource(), e.getOccurredAt(), e.getDetails());
                if (!e.getPrevHash().equals(expectedPrev) || !e.getHash().equals(recomputed)) {
                    return new VerificationResult(false, checked, e.getEventId());
                }
                expectedPrev = e.getHash();
                lastId = e.getId();
                checked++;
            }
        }
        return new VerificationResult(true, checked, null);
    }

    private String toJson(Map<String, String> details) {
        try {
            return objectMapper.writeValueAsString(details == null ? Map.of() : details);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static Instant parseTime(String timestamp) {
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private static String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
