package com.project.convoback.model.security;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "pending_registrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pending_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_pending_username",
                        columnNames = "username"
                )
        }
)
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastOtpSentAt() {
        return lastOtpSentAt;
    }

    public void setLastOtpSentAt(LocalDateTime lastOtpSentAt) {
        this.lastOtpSentAt = lastOtpSentAt;
    }
}
