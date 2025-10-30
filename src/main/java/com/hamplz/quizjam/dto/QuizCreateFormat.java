package com.hamplz.quizjam.dto;

public record QuizCreateFormat(
    String title,
    String type,
    String difficulty,
    String questionCount,
    String timeMinutes
) {
}
