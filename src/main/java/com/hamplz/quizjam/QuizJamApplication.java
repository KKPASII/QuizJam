package com.hamplz.quizjam;

import com.hamplz.quizjam.auth.dto.KakaoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KakaoProperties.class)
public class QuizJamApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizJamApplication.class, args);
	}

}
