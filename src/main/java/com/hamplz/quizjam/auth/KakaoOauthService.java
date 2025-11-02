package com.hamplz.quizjam.auth;

import com.hamplz.quizjam.auth.dto.KakaoProperties;
import com.hamplz.quizjam.auth.dto.KakaoTokenCreationRequest;
import com.hamplz.quizjam.auth.dto.KakaoTokenResponse;
import com.hamplz.quizjam.auth.dto.KakaoUserInfoResponse;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class KakaoOauthService {
    private static final String AUTHORIZATION_PATH = "https://kauth.kakao.com/oauth/authorize";

    private final KakaoApiClient kakaoApiClient;
    private final KakaoProperties kakaoProperties;
    private final UserService userService;

    KakaoOauthService(KakaoApiClient kakaoApiClient, KakaoProperties kakaoProperties, UserService userService) {
        this.kakaoApiClient = kakaoApiClient;
        this.kakaoProperties = kakaoProperties;
        this.userService = userService;
    }

    public String getKakaoLoginUrl() {
        return UriComponentsBuilder
                .fromUriString(AUTHORIZATION_PATH)
                .queryParam("response_type", "code")
                .queryParam("client_id", kakaoProperties.clientId())
                .queryParam("redirect_uri", kakaoProperties.redirectUri())
                .queryParam("scope", "profile_nickname")
                .toUriString();
    }

    /**
     * 인가 코드를 받아 카카오 로그인을 처리하고, 우리 서비스의 회원 정보를 반환합니다.
     * @param code 카카오로부터 받은 인가 코드
     * @return User 비스의 회원 엔티티
     */
    @Transactional
    public User login(String code) {
        KakaoTokenCreationRequest tokenRequest = new KakaoTokenCreationRequest(
                "authorization_code",
                kakaoProperties.clientId(),
                kakaoProperties.redirectUri(),
                code
        );
        KakaoTokenResponse tokenResponse = kakaoApiClient.createToken(tokenRequest);

        KakaoUserInfoResponse kakaoUserInfo = kakaoApiClient.getUserInfo(tokenResponse.accessToken());
        Long kakaoUserId = kakaoUserInfo.id();
        String nickname = Optional.ofNullable(kakaoUserInfo.kakaoAccount())
            .map(KakaoUserInfoResponse.KakaoAccount::profile)
            .map(KakaoUserInfoResponse.KakaoProfile::nickname)
            .orElse("Guest");

        return userService.findUserByKakaoId(kakaoUserId, nickname);
    }
}
