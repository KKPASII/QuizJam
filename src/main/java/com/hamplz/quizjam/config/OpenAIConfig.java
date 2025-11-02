package com.hamplz.quizjam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Bean
    public OpenAiService openAiService() {
        // 환경 변수에 저장된 OPENAI_API_KEY 사용
        String apiKey = System.getenv("OPENAI_API_KEY");
        return new OpenAiService(apiKey);
    }

    private class OpenAiService {
    }
}
