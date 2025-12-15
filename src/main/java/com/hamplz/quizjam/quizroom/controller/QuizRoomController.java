package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.quizroom.dto.CreateRoomRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class QuizRoomController {
    private final QuizRoomSerivce quizRoomService;

    public QuizRoomController(QuizRoomSerivce quizRoomService) {
        this.quizRoomService = quizRoomService;
    }

    /** 방 생성 */
    @PostMapping
    public ResponseEntity<QuizRoomResponse> create(
        @LoginUser Long userId,
        CreateRoomRequest createRoomRequest
    ) {
        quizRoomService.createRoom(userId, createRoomRequest.quizId());
        return null;
    }

    /** inviteCode로 방 조회 */
    @GetMapping("/code/{inviteCode}")
    public ResponseEntity<QuizRoomResponse> getByInviteCode(
            @PathVariable String inviteCode
    ) {
        return ResponseEntity.ok(
                quizRoomService.getRoomByInviteCode(inviteCode)
        );
    }

    /** 익명 참가자 입장 */
    @PostMapping("/join")
    public ResponseEntity<QuizRoomResponse> join(
            @RequestBody JoinRequest request
    ) {
        return ResponseEntity.ok(
                quizRoomService.join(request.inviteCode(), request.nickname())
        );
    }

    /** 방 퀴즈 변경 */
    @PutMapping("/{roomId}/quiz")
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
        @RequestParam("roomId") Long roomId
    ) {
        quizRoomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
