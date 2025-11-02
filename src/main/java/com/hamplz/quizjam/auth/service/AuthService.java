package com.hamplz.quizjam.auth.service;

import com.hamplz.quizjam.util.JwtUtil;
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

    public UsernamePasswordAuthenticationToken authenticationToken(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(BEARER_PREFIX.length());

            try {
                if (jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userId = jwtUtil.getUserId(jwt);
                    Long principalUserId = Long.parseLong(userId);

                    return new UsernamePasswordAuthenticationToken(
                            principalUserId, null, Collections.emptyList());
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                throw new RuntimeException("만료된 토큰");
            } catch (JwtException | NumberFormatException e) {
                throw new RuntimeException("유효하지 않은 토큰");
            }
        }
        return null;
    }
}
