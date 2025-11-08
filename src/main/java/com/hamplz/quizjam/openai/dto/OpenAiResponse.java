package com.hamplz.quizjam.openai.dto;

import java.util.List;
import java.util.Map;

public record OpenAiResponse(
        List<QuestionForm> questions,
        List<AnswerForm> answers
) {
    public record QuestionForm(
            int id,
            int quiz_id,
            String question_text,
            Map<String, String> options,
            String hint
    ) {}

    public record AnswerForm(
            int id,
            int question_id,
            String correct_answer,
            String explanation
    ) {}
}
