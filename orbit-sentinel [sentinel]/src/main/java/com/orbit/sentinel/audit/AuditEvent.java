package com.orbit.sentinel.audit;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.Immutable;

/** One immutable audit record. There are deliberately no setters. */
@Entity
@Immutable
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false, unique = true)
    private String eventId;

    @Column(nullable = false, updatable = false)
    private String type;

    @Column(nullable = false, updatable = false)
    private String actor;

    @Column(nullable = false, updatable = false)
    private String source;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "prev_hash", nullable = false, updatable = false)
    private String prevHash;

    @Column(nullable = false, updatable = false)
    private String hash;

    protected AuditEvent() {
    }

    public AuditEvent(String eventId, String type, String actor, String source, Instant occurredAt,
                      Instant receivedAt, String details, String prevHash, String hash) {
        this.eventId = eventId;
        this.type = type;
        this.actor = actor;
        this.source = source;
        this.occurredAt = occurredAt;
        this.receivedAt = receivedAt;
        this.details = details;
        this.prevHash = prevHash;
        this.hash = hash;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getType() { return type; }
    public String getActor() { return actor; }
    public String getSource() { return source; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public String getDetails() { return details; }
    public String getPrevHash() { return prevHash; }
    public String getHash() { return hash; }
}
