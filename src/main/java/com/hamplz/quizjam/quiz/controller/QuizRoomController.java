package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.quiz.dto.QuizRoomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizRoom")
public class QuizRoomController {

    @PostMapping
    public ResponseEntity<QuizRoomResponse> createQuizRoom(
    ) {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizRoomResponse> getQuizRoom(

    ) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<QuizRoomResponse> deleteQuizRoom(

    ) {
        return null;
    }
    //TODO: 퀴즈룸 생성, 조회, 삭제 만들기
}
