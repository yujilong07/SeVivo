package com.meilong.messenger.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "health_checks", indexes = {
        @Index(name = "idx_health_checks_target_checked_at", columnList = "target_id, checked_at")
})
public class HealthCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private Target target;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    private Integer statusCode;

    @Column(nullable = false)
    private long responseTimeMs;

    @Column(nullable = false)
    private boolean successful;

    @Column(length = 1000)
    private String errorMessage;

    protected HealthCheck() {
    }

    public HealthCheck(Target target, Instant checkedAt, Integer statusCode, long responseTimeMs,
                       boolean successful, String errorMessage) {
        this.target = target;
        this.checkedAt = checkedAt;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public Target getTarget() {
        return target;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
