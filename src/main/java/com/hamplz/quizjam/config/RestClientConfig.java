package com.hamplz.quizjam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class RestClientConfig {
    @Bean
    public RestClient kakaoRestClient() {
        return RestClient.builder().build();
    }
}
