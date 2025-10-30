package com.hamplz.quizjam.dto;

public record QuizQuestion(
    String question,
    String options,
    String hint
) {
}
