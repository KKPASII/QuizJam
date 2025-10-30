package com.hamplz.quizjam.dto;

public record QuizRoomResponse(
    Long roomId,
    String inviteCode,
    String hostUserId
) {
}
