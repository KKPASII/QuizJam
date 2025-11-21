package com.hamplz.quizjam.openai.dto;

import java.util.List;

public record OpenAiRequest(
        String model,
        List<Message> messages,
        double temperature,
        int max_tokens,
        ResponseFormat response_format
) {
    public record Message(
            String role,     // "system", "user", "assistant"
            String content   // 메시지 내용
    ) {}

    public record ResponseFormat(String type) {}
}
