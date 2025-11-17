package com.hamplz.quizjam.openai.dto;

import java.util.List;
import java.util.Map;

public record OpenAiResponse(
        List<QuestionForm> questions,
        List<AnswerForm> answers
) {
    public record QuestionForm(
            String questionText,
            Map<String, String> options,
            String hint
    ) {}

    public record AnswerForm(
            String correctAnswer,
            String explanation
    ) {}
}
