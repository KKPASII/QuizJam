package com.hamplz.quizjam.quizroom.dto;

public record QuizResultSubmitRequest(
    Long roomId,
    int score
) {
}
