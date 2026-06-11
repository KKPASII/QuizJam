package com.hamplz.quizjam.quizroom.dto;

public record JoinRoomResponse(
    QuizRoomResponse room,
    Long participantId,
    String nickname
) {
}
