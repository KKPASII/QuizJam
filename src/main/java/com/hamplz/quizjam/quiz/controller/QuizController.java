package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.quiz.dto.*;
import com.hamplz.quizjam.quiz.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping(
        consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    public ResponseEntity<QuizResponse> createQuiz(
        @LoginUser Long userId,
        @RequestPart("quiz") QuizCreateFormat quizCreateFormat,
        @RequestPart(value = "file") MultipartFile file
    ) throws Exception {
        log.info("📥 퀴즈 생성 요청: {}", quizCreateFormat.title());
        QuizResponse quizQuestions = quizService.createQuiz(userId, quizCreateFormat, file);
        log.info("✅ 퀴즈 생성 완료");

        return ResponseEntity.ok(quizQuestions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> getQuiz(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<QuizResponse>> getQuizzes(
        @LoginUser Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(quizService.getQuizzes(userId, page, size));
    }


    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuizQuestions(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(quizService.getQuizQuestions(id));
    }

    @GetMapping("/{id}/answers")
    public ResponseEntity<List<QuizAnswer>> getQuizAnswer(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(quizService.getQuizAnswers(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(
        @PathVariable Long id
    ) {
        quizService.deleteQuiz(id);

        return ResponseEntity.noContent().build();
    }
}