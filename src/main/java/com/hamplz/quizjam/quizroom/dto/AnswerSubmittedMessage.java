package com.hamplz.quizjam.quizroom.dto;

public record AnswerSubmittedMessage(
    Long roomId,
    int questionIndex,
    Long questionId,
    Long participantId,
    int submittedCount
) {
}
