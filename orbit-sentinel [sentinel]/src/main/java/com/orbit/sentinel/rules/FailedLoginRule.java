package com.orbit.sentinel.rules;

import com.orbit.sentinel.alerts.Severity;
import com.orbit.sentinel.audit.AuditEvent;
import com.orbit.sentinel.audit.AuditEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FailedLoginRule implements DetectionRule {

    private final AuditEventRepository repository;
    private final int threshold;
    private final Duration window;

    public FailedLoginRule(AuditEventRepository repository,
                           @Value("${sentinel.rules.failed-login.threshold:5}") int threshold,
                           @Value("${sentinel.rules.failed-login.window-seconds:120}") long windowSeconds) {
        this.repository = repository;
        this.threshold = threshold;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    public String name() {
        return "FAILED_LOGIN_BURST";
    }

    @Override
    public Severity severity() {
        return Severity.HIGH;
    }

    @Override
    public Duration dedupeWindow() {
        return window;
    }

    @Override
    public Optional<String> evaluate(AuditEvent event) {
        if (!"LOGIN_FAILED".equals(event.getType())) {
            return Optional.empty();
        }
        Instant from = event.getOccurredAt().minus(window);
        long count = repository.countByTypeAndActorAndOccurredAtGreaterThanEqual(
                "LOGIN_FAILED", event.getActor(), from);
        if (count >= threshold) {
            return Optional.of(count + " failed logins for " + event.getActor()
                    + " within " + window.toSeconds() + " seconds");
        }
        return Optional.empty();
    }
}
