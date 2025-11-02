package com.hamplz.quizjam.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private static final long ACCESS_TOKEN_MAX_AGE = 60 * 30;
    private static final long REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 30; // 30일

    /**
     * ✅ 액세스 토큰 쿠키 설정
     */
    public static void setAccessTokenCookie(HttpServletResponse response, String token) {
        setHttpOnlyCookie(response, "accessToken", token, ACCESS_TOKEN_MAX_AGE);
    }

    /**
     * ✅ 리프레시 토큰 쿠키 설정
     */
    public static void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setHttpOnlyCookie(response, "refreshToken", token, REFRESH_TOKEN_MAX_AGE);
    }

    /**
     * ✅ 공통 HttpOnly 쿠키 생성 (SameSite=None)
     */
    private static void setHttpOnlyCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        try {
            ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)          // ⚠️ 운영 시 true로 변경
                .sameSite("None")       // ✅ CORS 허용
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();

            response.addHeader("Set-Cookie", cookie.toString());
            logger.debug("✅ Set cookie: {} (maxAge={}s, secure={})", name, maxAgeSeconds, false);

        } catch (Exception e) {
            logger.error("❌ Error setting cookie: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ 쿠키 삭제
     */
    public static void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("🧹 Cleared cookie: {}", name);
    }
}
