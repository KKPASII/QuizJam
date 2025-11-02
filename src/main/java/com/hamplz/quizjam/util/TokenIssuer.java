package com.hamplz.quizjam.util;

import com.hamplz.quizjam.auth.RefreshTokenRepository;
import com.hamplz.quizjam.auth.dto.AuthToken;
import com.hamplz.quizjam.auth.entity.DeviceType;
import com.hamplz.quizjam.auth.entity.RefreshToken;
import com.hamplz.quizjam.user.User;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TokenIssuer {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenIssuer(JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthToken issue(User user, DeviceType deviceType, String userAgent) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);

        Claims claims = jwtUtil.getClaims(refreshTokenValue);
        String tokenId = claims.getId();
        LocalDateTime expiryDate = claims.getExpiration().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken refreshToken = RefreshToken.create(tokenId, user, deviceType, userAgent);
        refreshTokenRepository.save(refreshToken);

        return new AuthToken(accessToken, refreshTokenValue);
    }
}
