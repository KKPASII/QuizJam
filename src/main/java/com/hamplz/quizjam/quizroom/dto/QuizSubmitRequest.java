package com.hamplz.quizjam.quizroom.dto;

public record QuizSubmitRequest(
    Long roomId,
    Long questionId,
    String answer
) {
}
