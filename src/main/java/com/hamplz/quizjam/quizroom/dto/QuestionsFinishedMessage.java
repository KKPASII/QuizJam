package com.hamplz.quizjam.quizroom.dto;

public record QuestionsFinishedMessage(
    Long roomId,
    Long quizId,
    int questionCount
) {
}
