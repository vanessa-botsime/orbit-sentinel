package com.orbit.sentinel.rules;

import com.orbit.sentinel.alerts.Severity;
import com.orbit.sentinel.audit.AuditEvent;
import java.time.Duration;
import java.util.Optional;

/** To add a detection rule: implement this interface and annotate the class with @Component. */
public interface DetectionRule {

    String name();

    Severity severity();

    /** Minimum gap between two alerts of this rule for the same actor. Duration.ZERO means alert every time. */
    Duration dedupeWindow();

    /** Returns an alert message if the event triggers this rule. */
    Optional<String> evaluate(AuditEvent event);
}
