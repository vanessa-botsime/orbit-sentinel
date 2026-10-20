package com.orbit.sentinel.rules;

import com.orbit.sentinel.alerts.Alert;
import com.orbit.sentinel.alerts.AlertNotifier;
import com.orbit.sentinel.alerts.AlertRepository;
import com.orbit.sentinel.audit.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final List<DetectionRule> rules;
    private final AlertRepository alerts;
    private final AlertNotifier notifier;

    public RuleEngine(List<DetectionRule> rules, AlertRepository alerts, AlertNotifier notifier) {
        this.rules = rules;
        this.alerts = alerts;
        this.notifier = notifier;
    }

    public void evaluate(AuditEvent event) {
        for (DetectionRule rule : rules) {
            try {
                Optional<String> message = rule.evaluate(event);
                if (message.isEmpty()) {
                    continue;
                }
                if (!rule.dedupeWindow().isZero() && alerts.existsByRuleNameAndActorAndCreatedAtAfter(
                        rule.name(), event.getActor(), Instant.now().minus(rule.dedupeWindow()))) {
                    continue;
                }
                Alert alert = alerts.save(new Alert(
                        rule.name(), rule.severity(), event.getActor(), message.get(), event.getEventId()));
                if (notifier.send(alert)) {
                    alert.markDelivered();
                    alerts.save(alert);
                }
            } catch (Exception e) {
                log.error("Rule {} failed: {}", rule.name(), e.getClass().getSimpleName());
            }
        }
    }
}
