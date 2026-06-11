package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.RoomEventMessage;
import com.hamplz.quizjam.quizroom.service.ParticipantSessionRegistry;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class QuizRoomWebSocketController {

    private final QuizRoomSerivce quizRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ParticipantSessionRegistry participantSessionRegistry;

    public QuizRoomWebSocketController(
        QuizRoomSerivce quizRoomService,
        SimpMessagingTemplate messagingTemplate,
        ParticipantSessionRegistry participantSessionRegistry
    ) {
        this.quizRoomService = quizRoomService;
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
}
