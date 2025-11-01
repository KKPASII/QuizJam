package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.quiz.dto.QuizAnswer;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.dto.QuizQuestion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    // 문제 만들기 & 문제 반환
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<QuizQuestion> createQuiz(
        @RequestPart("quiz") QuizCreateFormat quizFormat,
        @RequestPart(value = "file", required = false) MultipartFile file
    ) {
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