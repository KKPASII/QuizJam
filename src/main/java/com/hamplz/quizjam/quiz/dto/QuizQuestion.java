package com.hamplz.quizjam.quiz.dto;

public record QuizQuestion(
    String questionText,
    String options,
    String hint
) {
}
