package com.hamplz.quizjam.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private static final int ACCESS_TOKEN_MAX_AGE = 60 * 30;
    private static final int REFRESH_TOKEN_MAX_AGE = 60 * 60 * 24 * 30; // 30일
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * ✅ 액세스 토큰 쿠키 생성 (SameSite=None, HttpOnly)
     */
    public static Cookie createAccessTokenCookie(String token) throws UnsupportedEncodingException {
        String encodedValue = URLEncoder.encode("Bearer " + token, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("accessToken", encodedValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬에서는 false, HTTPS 환경이면 true
        return createCookie("accessToken", BEARER_PREFIX + token, ACCESS_TOKEN_MAX_AGE);
    }

    public static Cookie createRefreshTokenCookie(String token) throws UnsupportedEncodingException {
        return createCookie("refreshToken", BEARER_PREFIX + token, REFRESH_TOKEN_MAX_AGE);
    }

    private static Cookie createCookie(String name, String value, int maxAge) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie(name, encoded);
        cookie.setHttpOnly(!"accessToken".equals(name));
        cookie.setSecure(false);    // HTTPS 환경: true
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * ✅ SameSite=None 쿠키 헤더 강제 추가
     */
    public static void addCookieWithSameSite(HttpServletResponse response, Cookie cookie) {
        String headerValue = String.format(
            "%s=%s; Max-Age=%d; Path=%s; HttpOnly; SameSite=None%s",
            cookie.getName(),
            cookie.getValue(),
            cookie.getMaxAge(),
            cookie.getPath(),
            cookie.getSecure() ? "; Secure" : ""
        );
        response.addHeader("Set-Cookie", headerValue);
        logger.debug("✅ Set-Cookie header added: {}", headerValue);
    }


    public static Cookie deleteAccessTokenCookie() {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
