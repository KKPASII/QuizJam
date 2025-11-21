package com.hamplz.quizjam.quiz.dto;

import java.util.Map;

public record QuizQuestion(
    String questionText,
    Map<String, String> options,
    String hint
) {
}