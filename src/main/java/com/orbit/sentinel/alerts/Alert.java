package com.orbit.sentinel.alerts;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean delivered;

    protected Alert() {
    }

    public Alert(String ruleName, Severity severity, String actor, String message, String eventId) {
        this.ruleName = ruleName;
        this.severity = severity;
        this.actor = actor;
        this.message = message;
        this.eventId = eventId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void markDelivered() { this.delivered = true; }

    public Long getId() { return id; }
    public String getRuleName() { return ruleName; }
    public Severity getSeverity() { return severity; }
    public String getActor() { return actor; }
    public String getMessage() { return message; }
    public String getEventId() { return eventId; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isDelivered() { return delivered; }
}
