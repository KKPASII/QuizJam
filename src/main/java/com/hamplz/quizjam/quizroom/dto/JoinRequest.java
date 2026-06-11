package com.hamplz.quizjam.quizroom.dto;

public record JoinRequest(
    String inviteCode,
    String nickname
) {
}
