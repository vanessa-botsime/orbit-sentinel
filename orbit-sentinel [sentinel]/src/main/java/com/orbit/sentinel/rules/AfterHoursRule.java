package com.orbit.sentinel.rules;

import com.orbit.sentinel.alerts.Severity;
import com.orbit.sentinel.audit.AuditEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AfterHoursRule implements DetectionRule {

    private final ZoneId zone;
    private final int startHour;
    private final int endHour;

    public AfterHoursRule(@Value("${sentinel.rules.after-hours.zone:Africa/Johannesburg}") String zone,
                          @Value("${sentinel.rules.after-hours.start-hour:22}") int startHour,
                          @Value("${sentinel.rules.after-hours.end-hour:5}") int endHour) {
        this.zone = ZoneId.of(zone);
        this.startHour = startHour;
        this.endHour = endHour;
    }

    @Override
    public String name() {
        return "AFTER_HOURS_LOGIN";
    }

    @Override
    public Severity severity() {
        return Severity.MEDIUM;
    }

    @Override
    public Duration dedupeWindow() {
        return Duration.ofHours(1);
    }

    @Override
    public Optional<String> evaluate(AuditEvent event) {
        if (!"LOGIN_SUCCESS".equals(event.getType())) {
            return Optional.empty();
        }
        if (isAfterHours(event.getOccurredAt())) {
            return Optional.of(event.getActor() + " logged in outside normal hours ("
                    + event.getOccurredAt().atZone(zone).toLocalTime().withNano(0) + " " + zone + ")");
        }
        return Optional.empty();
    }

    /** True when the local hour falls in the window from startHour to endHour (which may wrap past midnight). */
    public boolean isAfterHours(Instant at) {
        int hour = at.atZone(zone).getHour();
        if (startHour > endHour) {
            return hour >= startHour || hour < endHour;
        }
        return hour >= startHour && hour < endHour;
    }
}
