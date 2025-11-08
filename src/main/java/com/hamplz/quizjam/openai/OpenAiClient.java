package com.hamplz.quizjam.openai;

import com.hamplz.quizjam.openai.dto.OpenAiRequest;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {

    @Value("${openai.api-url}")
    private String apiUrl;

    private final RestClient openAiRestClient;

    public OpenAiClient(@Qualifier("openAiRestClient") RestClient openAiRestClient) {
        this.openAiRestClient = openAiRestClient;
    }

    /**
     * OpenAI ChatCompletion 호출 메서드
     * OpenAI Chat Completions → 원본 JSON 문자열로 반환
    */
    public String sendPromptAsString(OpenAiRequest request) {
        return openAiRestClient.post()
                .uri(apiUrl)
                .body(request)
                .retrieve()
                .body(String.class);
    }
}
