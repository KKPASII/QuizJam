package com.hamplz.quizjam.quiz.dto;

public record QuizRoomResponse(
    Long roomId,
    String inviteCode,
    String hostUserId
) {
}
