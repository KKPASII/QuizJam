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

    public static Cookie createAccessTokenCookie(String token) throws UnsupportedEncodingException {
        String encodedValue = URLEncoder.encode("Bearer " + token, StandardCharsets.UTF_8);
        Cookie cookie = new Cookie("accessToken", encodedValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 환경이면 true로 변경
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE);
        return cookie;
    }

    public static Cookie deleteAccessTokenCookie() {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
