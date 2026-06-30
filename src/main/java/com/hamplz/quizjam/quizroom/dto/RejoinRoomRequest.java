package com.hamplz.quizjam.quizroom.dto;

public record RejoinRoomRequest(
    Long roomId,
    Long participantId
) {
}
