package com.hamplz.quizjam.auth.controller;

import com.hamplz.quizjam.auth.KakaoOauthService;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.util.CookieUtil;
import com.hamplz.quizjam.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;

@Controller
public class LoginController {
    private final KakaoOauthService kakaoOauthService;
    private final JwtUtil jwtUtil;

    public LoginController(JwtUtil jwtUtil, KakaoOauthService kakaoOauthService) {
        this.jwtUtil = jwtUtil;
        this.kakaoOauthService = kakaoOauthService;
    }

    @GetMapping("/api/kakao/login")
    public ResponseEntity<String> redirectToKakao() {
        String kakaoLoginUrl = kakaoOauthService.getKakaoLoginUrl();
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws UnsupportedEncodingException {

        User user = kakaoOauthService.login(code);  // ✅ 카카오 로그인 처리
        String token = jwtUtil.generateAccessToken(user); // ✅ JWT 발급
        Cookie cookie = CookieUtil.createAccessTokenCookie(token);
        CookieUtil.addCookieWithSameSite(response, cookie);

        return "redirect:http://localhost:5173/";
    }
}
