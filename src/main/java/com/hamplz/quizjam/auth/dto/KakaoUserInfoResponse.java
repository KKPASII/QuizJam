package com.hamplz.quizjam.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            @JsonProperty("profile") KakaoProfile profile
    ) {}

    public record KakaoProfile(
            @JsonProperty("nickname") String nickname
    ) {}
}