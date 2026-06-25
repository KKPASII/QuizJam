package com.hamplz.quizjam.quizroom.dto;

public record ParticipantRankingResponse(
    int rank,
    Long participantId,
    String nickname,
    int score
) {
}
