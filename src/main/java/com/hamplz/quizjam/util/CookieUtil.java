package com.hamplz.quizjam.util;

import jakarta.servlet.http.Cookie;
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

    public static Cookie createAccessTokenCookie(String token) throws UnsupportedEncodingException {
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

    public static Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
