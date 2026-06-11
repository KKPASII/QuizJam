package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.quizroom.dto.CreateRoomRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class QuizRoomController {
    private final QuizRoomSerivce quizRoomService;

    public QuizRoomController(QuizRoomSerivce quizRoomService) {
        this.quizRoomService = quizRoomService;
    }

    @PostMapping
    public ResponseEntity<QuizRoomResponse> create(
        @LoginUser Long userId,
        @Valid @RequestBody CreateRoomRequest request
    ) {
        return ResponseEntity.ok(quizRoomService.createRoom(userId, request));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<QuizRoomResponse> get(@PathVariable Long roomId) {
        return ResponseEntity.ok(quizRoomService.getRoom(roomId));
    }

    @GetMapping("/code/{inviteCode}")
    public ResponseEntity<QuizRoomResponse> getByInviteCode(@PathVariable String inviteCode) {
        return ResponseEntity.ok(quizRoomService.getRoomByInviteCode(inviteCode));
    }

    @PostMapping("/join")
    public ResponseEntity<JoinRoomResponse> join(@RequestBody JoinRequest request) {
        return ResponseEntity.ok(quizRoomService.join(request.inviteCode(), request.nickname()));
    }

    @PatchMapping("/{roomId}/quiz")
    public ResponseEntity<QuizRoomResponse> updateQuiz(
        @LoginUser Long userId,
        @PathVariable Long roomId,
        @Valid @RequestBody CreateRoomRequest request
    ) {
        return ResponseEntity.ok(quizRoomService.updateRoomQuiz(roomId, userId, request));
    }

    @PutMapping("/{roomId}/start")
    public ResponseEntity<QuizRoomResponse> start(
        @PathVariable Long roomId,
        @LoginUser Long userId
    ) {
        return ResponseEntity.ok(quizRoomService.startGame(roomId, userId));
    }

    @PutMapping("/{roomId}/finish")
    public ResponseEntity<QuizRoomResponse> finish(
        @PathVariable Long roomId,
        @LoginUser Long userId
    ) {
        return ResponseEntity.ok(quizRoomService.finishGame(roomId, userId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(
        @LoginUser Long userId,
        @PathVariable Long roomId
    ) {
        quizRoomService.deleteRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }
}
