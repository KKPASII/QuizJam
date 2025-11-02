package com.hamplz.quizjam.auth.entity;

import com.hamplz.quizjam.user.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {
    @Id
    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    private boolean revoked = false;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    private String userAgent;

    @CreatedDate
    private LocalDateTime createdAt;

    protected RefreshToken() {
    }

    private static final long DEFAULT_EXPIRY_DAYS = 30;

    private RefreshToken(String tokenId, User user,  DeviceType deviceType, String userAgent) {
        this.tokenId = tokenId;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS);;
        this.deviceType = deviceType;
        this.userAgent = userAgent;
    }

    public static RefreshToken create(String tokenId, User user, DeviceType deviceType, String userAgent) {
        return new RefreshToken(tokenId, user, deviceType, userAgent);
    }

    public User getUser() { return user; }

    public boolean isRevoked() { return revoked; }

    public DeviceType getDeviceType() { return deviceType; }

    public String getUserAgent() { return userAgent; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void revoke() {
        this.revoked = true;
    }
}
