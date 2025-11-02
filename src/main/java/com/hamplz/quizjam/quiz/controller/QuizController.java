package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.quiz.dto.QuizAnswer;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.dto.QuizQuestion;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.service.QuizGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizGenerationService quizGenerationService;

    public QuizController(QuizGenerationService quizGenerationService) {
        this.quizGenerationService = quizGenerationService;
    }

    // 문제 만들기 & 문제 반환
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<QuizQuestion> createQuiz(
        @RequestPart("quiz") QuizCreateFormat quizCreateFormat,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        File tempFile = File.createTempFile("upload-", ".pdf");
        file.transferTo(tempFile);

        Quiz quiz = quizGenerationService.generateQuizFromPdf(tempFile, quizCreateFormat);
        return null;
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