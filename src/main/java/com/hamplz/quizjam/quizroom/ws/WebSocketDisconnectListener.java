package com.hamplz.quizjam.quizroom.ws;

import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.dto.RoomEventMessage;
import com.hamplz.quizjam.quizroom.service.LeaveRoomResult;
import com.hamplz.quizjam.quizroom.service.ParticipantSessionRegistry;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
public class WebSocketDisconnectListener {

    private final ParticipantSessionRegistry participantSessionRegistry;
    private final QuizRoomSerivce quizRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketDisconnectListener(
        ParticipantSessionRegistry participantSessionRegistry,
        QuizRoomSerivce quizRoomService,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.participantSessionRegistry = participantSessionRegistry;
        this.quizRoomService = quizRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }

        Long participantId = participantSessionRegistry.getParticipantId(sessionId);
        Long roomId = participantSessionRegistry.getRoomId(sessionId);
        participantSessionRegistry.remove(sessionId);

        if (participantId == null || roomId == null) {
            return;
        }

        LeaveRoomResult result = quizRoomService.leaveAndCloseWaitingRoomIfNeeded(roomId, participantId);
        if (result.closed()) {
            participantSessionRegistry.removeRoom(roomId);
            messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                RoomEventMessage.of("ROOM_CLOSED", Map.of("roomId", roomId))
            );
            return;
        }

        QuizRoomResponse room = result.room();
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId,
            RoomEventMessage.of("ROOM_SNAPSHOT", room)
        );
    }
}
