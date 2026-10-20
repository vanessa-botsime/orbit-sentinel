package com.orbit.sentinel.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuditHasherTest {

    private static final Instant TIME = Instant.parse("2026-10-19T10:00:00Z");

    @Test
    void sameInputGivesSameHash() {
        String a = AuditHasher.hash(AuditHasher.GENESIS, "e1", "LOGIN_FAILED", "a@b.com", "orbit", TIME, "{}");
        String b = AuditHasher.hash(AuditHasher.GENESIS, "e1", "LOGIN_FAILED", "a@b.com", "orbit", TIME, "{}");
        assertEquals(a, b);
        assertEquals(64, a.length());
    }

    @Test
    void changingAnyFieldChangesTheHash() {
        String original = AuditHasher.hash(AuditHasher.GENESIS, "e1", "LOGIN_FAILED", "a@b.com", "orbit", TIME, "{}");
        String otherActor = AuditHasher.hash(AuditHasher.GENESIS, "e1", "LOGIN_FAILED", "x@y.com", "orbit", TIME, "{}");
        String otherPrev = AuditHasher.hash("1".repeat(64), "e1", "LOGIN_FAILED", "a@b.com", "orbit", TIME, "{}");
        assertNotEquals(original, otherActor);
        assertNotEquals(original, otherPrev);
    }
}
