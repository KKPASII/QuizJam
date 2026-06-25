package com.hamplz.quizjam.quizroom.dto;

public record QuestionClosedMessage(
    Long roomId,
    Long quizId,
    int questionIndex,
    int questionCount,
    Long questionId
) {
}
