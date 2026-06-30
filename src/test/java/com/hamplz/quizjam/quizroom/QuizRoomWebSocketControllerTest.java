package com.hamplz.quizjam.quizroom;

import com.hamplz.quizjam.quizroom.controller.QuizRoomWebSocketController;
import com.hamplz.quizjam.quizroom.dto.JoinRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.ParticipantResponse;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizStartRequest;
import com.hamplz.quizjam.quizroom.dto.QuizSubmitRequest;
import com.hamplz.quizjam.quizroom.dto.RejoinRoomRequest;
import com.hamplz.quizjam.quizroom.entity.QuizRoomStatus;
import com.hamplz.quizjam.quizroom.service.ParticipantSessionRegistry;
import com.hamplz.quizjam.quizroom.service.QuizPlayService;
import com.hamplz.quizjam.quizroom.service.QuizRoomCleanupService;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizRoomWebSocketControllerTest {

    private static final Long ROOM_ID = 1L;
    private static final Long HOST_USER_ID = 10L;
    private static final Long HOST_PARTICIPANT_ID = 100L;
    private static final Long GUEST_PARTICIPANT_ID = 200L;

    @Mock
    private QuizRoomSerivce quizRoomService;

    @Mock
    private QuizPlayService quizPlayService;

    @Mock
    private QuizRoomCleanupService quizRoomCleanupService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ParticipantSessionRegistry participantSessionRegistry;
    private QuizRoomWebSocketController controller;

    @BeforeEach
    void setUp() {
        participantSessionRegistry = new ParticipantSessionRegistry();
        controller = new QuizRoomWebSocketController(
            quizRoomService,
            quizPlayService,
            quizRoomCleanupService,
            messagingTemplate,
            participantSessionRegistry
        );
    }

    @Test
    @DisplayName("guest join registers websocket session to joined participant")
    void guestJoinRegistersSession() {
        QuizRoomResponse room = roomResponse();
        when(quizRoomService.join("ABCDEFGH", "guest"))
            .thenReturn(new JoinRoomResponse(room, GUEST_PARTICIPANT_ID, "guest"));

        JoinRoomResponse response = controller.joinRoom(new JoinRequest("ABCDEFGH", "guest"), "session-guest");

        assertThat(response.participantId()).isEqualTo(GUEST_PARTICIPANT_ID);
        assertThat(participantSessionRegistry.getRoomId("session-guest")).isEqualTo(ROOM_ID);
        assertThat(participantSessionRegistry.getParticipantId("session-guest")).isEqualTo(GUEST_PARTICIPANT_ID);
    }

    @Test
    @DisplayName("guest refresh rejoin registers new websocket session to existing participant")
    void guestRejoinRegistersNewSession() {
        QuizRoomResponse room = roomResponse();
        when(quizRoomService.reconnectParticipant(ROOM_ID, GUEST_PARTICIPANT_ID)).thenReturn(room);

        controller.rejoinRoom(new RejoinRoomRequest(ROOM_ID, GUEST_PARTICIPANT_ID), "session-refreshed");

        assertThat(participantSessionRegistry.getRoomId("session-refreshed")).isEqualTo(ROOM_ID);
        assertThat(participantSessionRegistry.getParticipantId("session-refreshed")).isEqualTo(GUEST_PARTICIPANT_ID);
        verify(quizRoomCleanupService).cancelCleanup(ROOM_ID);
    }

    @Test
    @DisplayName("host refresh rejoin registers new websocket session to host participant")
    void hostRejoinRegistersNewSession() {
        QuizRoomResponse room = roomResponse();
        when(quizRoomService.reconnectHost(ROOM_ID, HOST_USER_ID)).thenReturn(room);

        controller.joinHostRoom(new QuizStartRequest(ROOM_ID), "session-host", principal(HOST_USER_ID));

        assertThat(participantSessionRegistry.getRoomId("session-host")).isEqualTo(ROOM_ID);
        assertThat(participantSessionRegistry.getParticipantId("session-host")).isEqualTo(HOST_PARTICIPANT_ID);
        verify(quizRoomCleanupService).cancelCleanup(ROOM_ID);
    }

    @Test
    @DisplayName("answer submit after rejoin uses participant from websocket session registry")
    void submitAnswerAfterRejoinUsesRegisteredParticipant() {
        participantSessionRegistry.register("session-refreshed", ROOM_ID, GUEST_PARTICIPANT_ID);

        controller.submitAnswer(
            new QuizSubmitRequest(ROOM_ID, 300L, "A"),
            "session-refreshed"
        );

        verify(quizPlayService).submitAnswer(ROOM_ID, GUEST_PARTICIPANT_ID, 300L, "A");
    }

    private Principal principal(Long userId) {
        return () -> String.valueOf(userId);
    }

    private QuizRoomResponse roomResponse() {
        return new QuizRoomResponse(
            ROOM_ID,
            2L,
            "SQLD",
            5,
            "ABCDEFGH",
            "/rooms/join/ABCDEFGH",
            HOST_USER_ID,
            QuizRoomStatus.WAITING,
            5,
            List.of(
                new ParticipantResponse(HOST_PARTICIPANT_ID, HOST_USER_ID, "host", true, true, 0),
                new ParticipantResponse(GUEST_PARTICIPANT_ID, null, "guest", false, true, 0)
            )
        );
    }
}
