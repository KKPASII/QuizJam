package com.hamplz.quizjam.quizroom.dto;

public record JoinRequest(
    Long roomId,
    String inviteCode,
    String nickname
) {}
