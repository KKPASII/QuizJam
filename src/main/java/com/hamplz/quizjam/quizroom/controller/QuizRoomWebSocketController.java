package com.hamplz.quizjam.quizroom.controller;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.repository.QuizRoomRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class QuizRoomWebSocketController {

    private final QuizRoomRepository quizRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public QuizRoomWebSocketController(
        QuizRoomRepository quizRoomRepository,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.quizRoomRepository = quizRoomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/join") // → /app/room/join 호출됨
    public void joinRoom(JoinRequest request, @LoginUser Long userId) {

        QuizRoom room = quizRoomRepository.findById(request.roomId())
            .orElseThrow();

        Participant p = room.join(request.nickname(), null);

        quizRoomRepository.save(room);

        RoomUpdateMessage message = new RoomUpdateMessage(
            room.getId(),
            room.getHostParticipant().getNickname(),
            room.countParticipants(),
            room.getParticipantList().stream().map(Participant::getNickname).toList()
        );

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), message);
    }
}
