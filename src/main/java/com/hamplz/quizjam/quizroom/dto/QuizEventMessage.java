package com.hamplz.quizjam.quizroom.dto;

public record QuizEventMessage(
    String type,
    Object payload
) {
    public static QuizEventMessage of(String type, Object payload) {
        return new QuizEventMessage(type, payload);
    }
}
