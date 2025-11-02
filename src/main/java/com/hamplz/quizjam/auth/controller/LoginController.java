package com.hamplz.quizjam.auth.controller;

import com.hamplz.quizjam.auth.KakaoOauthService;
import com.hamplz.quizjam.auth.RefreshTokenRepository;
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
    private final RefreshTokenService refreshTokenService;

    public LoginController(JwtUtil jwtUtil, KakaoOauthService kakaoOauthService, TokenIssuer tokenIssuer, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.kakaoOauthService = kakaoOauthService;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> redirectToKakao() {
        String kakaoLoginUrl = kakaoOauthService.getKakaoLoginUrl();
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {

        User user = kakaoOauthService.login(code); // 카카오에서 유저 정보 받아오기
        ClientInfo clientInfo = getClientInfo(request);

        AuthToken tokenSet = tokenIssuer.issue(user, clientInfo.deviceType(), clientInfo.userAgent());

        response.addCookie(CookieUtil.createAccessTokenCookie(tokenSet.accessToken()));
        response.addCookie(CookieUtil.createRefreshTokenCookie(tokenSet.refreshToken()));

        return "redirect:http://localhost:5173/";
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<AuthTokenResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {
        String refreshToken = jwtUtil.extractRefreshToken(request);
        AuthToken newTokens = refreshTokenService.newTokenSet(refreshToken);

        response.addCookie(CookieUtil.createAccessTokenCookie(newTokens.accessToken()));
        response.addCookie(CookieUtil.createRefreshTokenCookie(newTokens.refreshToken()));

        return ResponseEntity.ok(new AuthTokenResponse(newTokens.accessToken()));
    }

    @PostMapping("/api/kakao/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 카카오 로그아웃

        return ResponseEntity.noContent().build();
    }

    private ClientInfo getClientInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        DeviceType deviceType = DeviceType.fromUserAgent(userAgent);
        return new ClientInfo(userAgent, deviceType);
    }
}
