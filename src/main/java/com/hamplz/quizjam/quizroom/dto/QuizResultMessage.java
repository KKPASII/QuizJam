package com.hamplz.quizjam.quizroom.dto;

import java.util.List;

public record QuizResultMessage(
    Long roomId,
    int submittedCount,
    int expectedCount,
    boolean finalized,
    List<ParticipantRankingResponse> rankings
) {
}
