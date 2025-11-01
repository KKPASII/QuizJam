package com.hamplz.quizjam.user.dto;

public record UserCreateRequest(
        String nickname,
        String kakaoId
) {
}
