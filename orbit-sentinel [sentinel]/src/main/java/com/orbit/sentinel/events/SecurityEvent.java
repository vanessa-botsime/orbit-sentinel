package com.orbit.sentinel.events;

import java.util.Map;

/** The event contract published by orbit-graduate-system. Timestamp is an ISO-8601 string. */
public record SecurityEvent(
        String eventId,
        String type,
        String actor,
        String source,
        String timestamp,
        Map<String, String> details) {
}
