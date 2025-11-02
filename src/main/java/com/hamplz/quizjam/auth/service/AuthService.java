package com.hamplz.quizjam.auth.service;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.UnAuthorizedException;
import com.hamplz.quizjam.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * ✅ JWT를 검증하고, 유효하면 Authentication 반환
     */
    public UsernamePasswordAuthenticationToken authenticateToken(String token) {
        if (token == null || token.isBlank()) return null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) return null;

        try {
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getUserId(token);
                Long principalUserId = Long.parseLong(userId);

                return new UsernamePasswordAuthenticationToken(
                    principalUserId, null, Collections.emptyList()
                );
            }
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ 만료된 JWT: {}", e.getMessage());
            throw new UnAuthorizedException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | NumberFormatException e) {
            log.warn("❌ 유효하지 않은 JWT: {}", e.getMessage());
            throw new UnAuthorizedException(ErrorCode.INVALID_TOKEN);
        }

        return null;
    }
}
