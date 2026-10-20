package com.orbit.sentinel.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.sentinel.audit.AuditEvent;
import com.orbit.sentinel.audit.AuditService;
import com.orbit.sentinel.rules.RuleEngine;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final RuleEngine ruleEngine;

    public EventConsumer(ObjectMapper objectMapper, AuditService auditService, RuleEngine ruleEngine) {
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.ruleEngine = ruleEngine;
    }

    @KafkaListener(topics = "orbit.events", groupId = "sentinel")
    public void onMessage(String payload) {
        SecurityEvent event;
        try {
            event = objectMapper.readValue(payload, SecurityEvent.class);
        } catch (Exception e) {
            log.warn("Skipping unreadable event: {}", e.getClass().getSimpleName());
            return;
        }
        if (event.eventId() == null || event.type() == null || event.actor() == null) {
            log.warn("Skipping event with missing fields");
            return;
        }
        Optional<AuditEvent> stored = auditService.record(event);
        stored.ifPresent(ruleEngine::evaluate);
    }
}
