package com.hamplz.quizjam.auth.dto;

public record KakaoTokenCreationRequest(
        String grantType,
        String clientId,
        String redirectUri,
        String code
) {
}