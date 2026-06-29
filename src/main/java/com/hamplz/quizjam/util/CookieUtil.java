package com.hamplz.quizjam.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private final long accessTokenMaxAge;
    private final long refreshTokenMaxAge;

    public CookieUtil(
        @Value("${jwt.access-token-validity-in-seconds}") long accessTokenMaxAge,
        @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenMaxAge
    ) {
        this.accessTokenMaxAge = accessTokenMaxAge;
        this.refreshTokenMaxAge = refreshTokenMaxAge;
    }

    /**
     * ✅ 액세스 토큰 쿠키 설정
     */
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        setHttpOnlyCookie(response, "accessToken", token, accessTokenMaxAge);
    }

    /**
     * ✅ 리프레시 토큰 쿠키 설정
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setHttpOnlyCookie(response, "refreshToken", token, refreshTokenMaxAge);
    }

    /**
     * ✅ 공통 HttpOnly 쿠키 생성 (SameSite=None)
     */
    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        try {
            ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)       // 개발은 HTTP라 false, ️운영 시 true로 변경 -> 운영은 HTTPS 필수
                .sameSite("Lax")     // localhost:5173 ↔ localhost:8081
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());
            logger.debug(
                "Set cookie: {} (maxAge={}s, secure=false, sameSite=Lax)",
                name,
                maxAgeSeconds
            );

        } catch (Exception e) {
            logger.error("❌ Error setting cookie: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ 쿠키 삭제
     */
    public void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax") // None: 다른 사이트 프론트에서도 API 호출 시 전송
            .path("/")
            .maxAge(0)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("🧹 Cleared cookie: {}", name);
    }
}
