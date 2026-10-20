package com.orbit.sentinel.audit;

public record VerificationResult(boolean valid, long checked, String firstBrokenEventId) {
}
