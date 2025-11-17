package com.hamplz.quizjam.quiz.dto;

public record QuizCreateFormat(
    String title,
    String type,
    String difficulty,
    int questionCount,
    long timeLimitMin
) {
}
