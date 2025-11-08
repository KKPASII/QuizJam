package com.hamplz.quizjam.openai.dto;

import java.util.List;

public record OpenAiRequest(
        String model,
        List<Message> messages,
        double temperature,
        int max_tokens
) {
    public record Message(
            String role,     // "system", "user", "assistant"
            String content   // 메시지 내용
    ) {}
}
