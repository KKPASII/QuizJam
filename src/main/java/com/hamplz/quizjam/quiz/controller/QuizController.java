package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizAnswer;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.dto.QuizQuestion;
import com.hamplz.quizjam.quiz.service.QuizGenerateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizGenerateService quizGenerationService;

    public QuizController(QuizGenerateService quizGenerationService) {
        this.quizGenerationService = quizGenerationService;
    }

    // 문제 생성 API
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<OpenAiResponse> createQuiz(
        @RequestPart("quiz") QuizCreateFormat quizCreateFormat,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {

        try {
            log.info("📥 퀴즈 생성 요청: {}", quizCreateFormat.title());
            OpenAiResponse response = quizGenerationService.generateQuizFromPdf(file);
            log.info("✅ 퀴즈 생성 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 퀴즈 생성 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 문제 조회하기
    @GetMapping("/{id}")
    public ResponseEntity<QuizQuestion> getQuiz(
        @PathVariable long id
    ) {
        return null;
    }

    // 정답 조회하기
    @GetMapping("/{id}/answer")
    public ResponseEntity<QuizAnswer> getQuizAnswer(
        @PathVariable long id
    ) {
        return null;
    }

    // 퀴즈 문제&정답 삭제하기
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(
        @PathVariable long id
    ) {
        return null;
    }
}