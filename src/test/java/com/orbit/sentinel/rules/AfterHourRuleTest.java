package com.orbit.sentinel.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AfterHoursRuleTest {

    // Johannesburg is UTC+2 all year, so 21:00Z is 23:00 local.
    private final AfterHoursRule rule = new AfterHoursRule("Africa/Johannesburg", 22, 5);

    @Test
    void lateEveningIsAfterHours() {
        assertTrue(rule.isAfterHours(Instant.parse("2026-10-19T21:00:00Z")));
    }

    @Test
    void earlyMorningIsAfterHours() {
        assertTrue(rule.isAfterHours(Instant.parse("2026-10-19T02:00:00Z")));
    }

    @Test
    void middayIsNormal() {
        assertFalse(rule.isAfterHours(Instant.parse("2026-10-19T10:00:00Z")));
    }

    @Test
    void windowEndsAtFiveLocal() {
        assertFalse(rule.isAfterHours(Instant.parse("2026-10-19T03:00:00Z")));
    }
}
