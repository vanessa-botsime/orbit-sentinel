package com.orbit.sentinel.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/** Builds the hash chain: each record's hash covers its own content plus the previous record's hash. */
public final class AuditHasher {

    public static final String GENESIS = "0".repeat(64);

    private AuditHasher() {
    }

    public static String hash(String prevHash, String eventId, String type, String actor,
                              String source, Instant occurredAt, String details) {
        String canonical = String.join("|",
                prevHash, eventId, type, actor, source,
                String.valueOf(occurredAt.toEpochMilli()), details);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
