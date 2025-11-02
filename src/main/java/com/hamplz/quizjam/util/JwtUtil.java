package com.hamplz.quizjam.util;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.UnAuthorizedException;
import com.hamplz.quizjam.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final Key secretKey;
    private final long accessTokenValidityInMs;
    private final long refreshTokenValidityInMs;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMs = accessTokenValidity * 1000;
        this.refreshTokenValidityInMs = refreshTokenValidity * 1000;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValidityInMs);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setId(UUID.randomUUID().toString())
                .claim("nickname", user.getNickname())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenValidityInMs);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
            .filter(cookie -> name.equalsIgnoreCase(cookie.getName()))
            .map(cookie -> decode(cookie.getValue()))
            .filter(value -> !value.isBlank())
            .findFirst()
            .orElse(null);
    }

    public String extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, "accessToken");
    }

    public String extractRefreshToken(HttpServletRequest request) {
        String token = extractCookieValue(request, "refreshToken");
        if (token == null) {
            throw new UnAuthorizedException(ErrorCode.MISSING_TOKEN);
        }
        return token;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid cookie encoding: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.info("⚠️ 만료된 토큰");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("❌ 유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new UnAuthorizedException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnAuthorizedException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }
}
