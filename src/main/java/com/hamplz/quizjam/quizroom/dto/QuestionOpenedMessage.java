package com.hamplz.quizjam.quizroom.dto;

import java.util.Map;

public record QuestionOpenedMessage(
    Long roomId,
    Long quizId,
    int questionIndex,
    int questionCount,
    Long questionId,
    String questionText,
    Map<String, String> options,
    String hint,
    long deadlineEpochMs
) {
}
