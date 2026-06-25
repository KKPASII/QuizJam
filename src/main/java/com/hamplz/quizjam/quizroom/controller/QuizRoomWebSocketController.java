package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizStartRequest;
import com.hamplz.quizjam.quizroom.dto.QuizSubmitRequest;
import com.hamplz.quizjam.quizroom.dto.RoomEventMessage;
import com.hamplz.quizjam.quizroom.service.ParticipantSessionRegistry;
import com.hamplz.quizjam.quizroom.service.QuizPlayService;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class QuizRoomWebSocketController {

    private final QuizRoomSerivce quizRoomService;
    private final QuizPlayService quizPlayService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ParticipantSessionRegistry participantSessionRegistry;

    public QuizRoomWebSocketController(
        QuizRoomSerivce quizRoomService,
        QuizPlayService quizPlayService,
        SimpMessagingTemplate messagingTemplate,
        ParticipantSessionRegistry participantSessionRegistry
    ) {
        this.quizRoomService = quizRoomService;
        this.quizPlayService = quizPlayService;
        this.messagingTemplate = messagingTemplate;
        this.participantSessionRegistry = participantSessionRegistry;
    }

    @MessageMapping("room.join")
    @SendToUser("/queue/room.joined")
    public JoinRoomResponse joinRoom(
        @Payload JoinRequest request,
        @Header("simpSessionId") String sessionId
    ) {
        JoinRoomResponse response = quizRoomService.join(request.inviteCode(), request.nickname());
        participantSessionRegistry.register(sessionId, response.room().roomId(), response.participantId());

        messagingTemplate.convertAndSend(
            "/topic/room/" + response.room().roomId(),
            RoomEventMessage.of("ROOM_SNAPSHOT", response.room())
        );

        return response;
    }

    @MessageMapping("room.start")
    public void startRoom(
        @Payload QuizStartRequest request,
        Principal principal
    ) {
        Long userId = requireLoginUser(principal);
        QuizRoomResponse room = quizRoomService.startGame(request.roomId(), userId);

        messagingTemplate.convertAndSend(
            "/topic/room/" + room.roomId(),
            RoomEventMessage.of("ROOM_STARTED", room)
        );
        quizPlayService.startGame(room);
    }

    @MessageMapping("quiz.submit")
    public void submitAnswer(
        @Payload QuizSubmitRequest request,
        @Header("simpSessionId") String sessionId
    ) {
        Long participantId = participantSessionRegistry.getParticipantId(sessionId);
        Long registeredRoomId = participantSessionRegistry.getRoomId(sessionId);
        if (participantId == null || registeredRoomId == null) {
            throw new IllegalStateException("Participant session is not registered.");
        }
        if (!registeredRoomId.equals(request.roomId())) {
            throw new IllegalStateException("Participant is not registered in requested room.");
        }

        quizPlayService.submitAnswer(
            request.roomId(),
            participantId,
            request.questionId(),
            request.answer()
        );
    }

    private Long requireLoginUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().startsWith("anonymous-")) {
            throw new IllegalStateException("Login WebSocket principal is required.");
        }
        return Long.valueOf(principal.getName());
    }
}
