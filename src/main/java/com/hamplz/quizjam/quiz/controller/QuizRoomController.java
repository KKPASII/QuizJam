package com.hamplz.quizjam.quiz.controller;

import com.hamplz.quizjam.quiz.dto.QuizRoomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizRoom")
public class QuizRoomController {

    @PostMapping
    public ResponseEntity<QuizRoomResponse> create(

    ) {
        return null;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<QuizRoomResponse> get(
        @RequestParam("roomId") Long id
    ) {
        return null;
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<QuizRoomResponse> update(
        @RequestParam("roomId") Long id
    ) {
        return null;
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<QuizRoomResponse> delete(
        @RequestParam("roomId") Long id
    ) {
        return null;
    }
    //TODO: 퀴즈룸 생성, 조회, 삭제 만들기
}
