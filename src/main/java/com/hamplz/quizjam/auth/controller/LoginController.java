package com.hamplz.quizjam.auth.controller;

import com.hamplz.quizjam.auth.KakaoOauthService;
import com.hamplz.quizjam.auth.dto.AuthToken;
import com.hamplz.quizjam.auth.dto.AuthTokenResponse;
import com.hamplz.quizjam.auth.dto.ClientInfo;
import com.hamplz.quizjam.auth.entity.DeviceType;
import com.hamplz.quizjam.auth.service.RefreshTokenService;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.util.CookieUtil;
import com.hamplz.quizjam.util.JwtUtil;
import com.hamplz.quizjam.util.TokenIssuer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;

@Controller
public class LoginController {
    private final KakaoOauthService kakaoOauthService;
    private final TokenIssuer tokenIssuer;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RefreshTokenService refreshTokenService;

    public LoginController(JwtUtil jwtUtil, KakaoOauthService kakaoOauthService, TokenIssuer tokenIssuer, CookieUtil cookieUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.kakaoOauthService = kakaoOauthService;
        this.tokenIssuer = tokenIssuer;
        this.cookieUtil = cookieUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> redirectToKakao() {
        String kakaoLoginUrl = kakaoOauthService.getKakaoLoginUrl();
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        @LoginUser Long userId
    ) {
        cookieUtil.clearCookie(response, "accessToken");
        cookieUtil.clearCookie(response, "refreshToken");

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {

        User user = kakaoOauthService.login(code);
        ClientInfo clientInfo = getClientInfo(request);

        AuthToken tokenSet = tokenIssuer.issue(user, clientInfo.deviceType(), clientInfo.userAgent());

        cookieUtil.setAccessTokenCookie(response, tokenSet.accessToken());
        cookieUtil.setRefreshTokenCookie(response, tokenSet.refreshToken());

        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", "http://localhost:5173/")
            .build();
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthTokenResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {
        String refreshToken = jwtUtil.extractRefreshToken(request);
        AuthToken newTokens = refreshTokenService.newTokenSet(refreshToken);

        cookieUtil.setAccessTokenCookie(response, newTokens.accessToken());
        cookieUtil.setRefreshTokenCookie(response, newTokens.refreshToken());

        return ResponseEntity.ok(new AuthTokenResponse(newTokens.accessToken()));
    }

    @PostMapping("/api/kakao/logout")
    public ResponseEntity<String> kakaoLogout(
            HttpServletResponse response
    ) {
        clearAuthCookies(response);
        return ResponseEntity.ok(kakaoOauthService.getKakaoLogoutUrl());
    }

    @GetMapping("/api/kakao/logout/callback")
    public ResponseEntity<Void> kakaoLogoutCallback(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", "http://localhost:5173/")
            .build();
    }

    private ClientInfo getClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        DeviceType deviceType = DeviceType.fromUserAgent(userAgent);
        return new ClientInfo(userAgent, deviceType);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        cookieUtil.clearCookie(response, "accessToken");
        cookieUtil.clearCookie(response, "refreshToken");
    }
}
