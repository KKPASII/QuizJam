package com.hamplz.quizjam.config;

import com.hamplz.quizjam.auth.service.AuthService;
import com.hamplz.quizjam.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 HttpOnly 쿠키에서 accessToken을 꺼내 JWT 검증 → 인증 컨텍스트 설정
 * principal은 Long(userId)로만 넣어, 컨트롤러에서 @LoginUser Long 으로 바로 받는다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {

            // 1️⃣ 토큰 추출 (HttpOnly 쿠키)
            String token = jwtUtil.extractAccessToken(request);
            log.info("📦 Extracted token: {}", token);
            // 2️⃣ AuthService에 검증 위임
            UsernamePasswordAuthenticationToken authentication =
                authService.authenticateToken(token);

            // 3️⃣ 인증객체를 SecurityContext에 저장
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ 인증 성공: {}", authentication.getPrincipal());
            }
        } catch (Exception ex) {
            log.debug("JWT 인증 실패: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
