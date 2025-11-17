package com.hamplz.quizjam.quiz.dto;

public record QuizResponse(
    long quizId,
    String title,
    String type,
    String difficulty,
    int questionCount,
    long timeLimitMin
) {
}