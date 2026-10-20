package com.orbit.sentinel.rules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.sentinel.alerts.Severity;
import com.orbit.sentinel.audit.AuditEvent;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RoleChangeRule implements DetectionRule {

    private final ObjectMapper objectMapper;

    public RoleChangeRule(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "ROLE_CHANGED";
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override
    public Duration dedupeWindow() {
        return Duration.ZERO;
    }

    @Override
    public Optional<String> evaluate(AuditEvent event) {
        if (!"ROLE_CHANGED".equals(event.getType())) {
            return Optional.empty();
        }
        try {
            Map<String, String> d = objectMapper.readValue(event.getDetails(), new TypeReference<Map<String, String>>() { });
            return Optional.of(event.getActor() + " changed the role of " + d.get("targetUser")
                    + " from " + d.get("oldRole") + " to " + d.get("newRole"));
        } catch (Exception e) {
            return Optional.of(event.getActor() + " changed a user's role");
        }
    }
}
