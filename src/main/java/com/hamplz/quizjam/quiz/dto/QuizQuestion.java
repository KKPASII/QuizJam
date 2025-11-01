package com.hamplz.quizjam.quiz.dto;

public record QuizQuestion(
    String question,
    String options,
    String hint
) {
}
