package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizRoom")
public class QuizRoomController {
    private final QuizRoomSerivce quizRoomSerivce;

    public QuizRoomController(QuizRoomSerivce quizRoomSerivce) {
        this.quizRoomSerivce = quizRoomSerivce;
    }

    @PostMapping
    public ResponseEntity<QuizRoomResponse> create(
        @LoginUser Long userId
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

    @PutMapping("/{roomId}/status")
    public ResponseEntity<QuizRoomResponse> updateStatus(
        @PathVariable Long roomId,
        @RequestParam boolean start
    ) {
        return ResponseEntity.ok(quizRoomService.updateStatus(roomId, start));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<QuizRoomResponse> delete(
        @RequestParam("roomId") Long id
    ) {
        quizRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }


    //TODO: 퀴즈룸 생성, 조회, 삭제 만들기
}
