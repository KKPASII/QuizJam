package com.hamplz.quizjam.auth.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public record KakaoProperties(
        String clientId,
        String redirectUri
) {
}
