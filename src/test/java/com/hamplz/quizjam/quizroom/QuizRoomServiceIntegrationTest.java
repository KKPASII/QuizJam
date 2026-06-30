package com.hamplz.quizjam.quizroom;

import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.quizroom.dto.CreateRoomRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.repository.QuizRoomRepository;
import com.hamplz.quizjam.quizroom.service.LeaveRoomResult;
import com.hamplz.quizjam.quizroom.service.QuizRoomSerivce;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QuizRoomServiceIntegrationTest {

    @Autowired
    private QuizRoomSerivce quizRoomService;

    @Autowired
    private QuizRoomRepository quizRoomRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    private User host;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quizRoomRepository.deleteAll();
        quizRepository.deleteAll();
        userRepository.deleteAll();

        host = userRepository.save(User.create("host", 10001L));
        quiz = quizRepository.save(Quiz.create(host, "SQLD", "MULTIPLE_CHOICE", "EASY", 5, 10L));
    }

    @Test
    @DisplayName("multiple guests can join and appear in room participants")
    void multipleGuestsJoinRoom() {
        QuizRoomResponse created = quizRoomService.createRoom(
            host.getId(),
            new CreateRoomRequest(quiz.getId(), 5)
        );

        JoinRoomResponse guest1 = quizRoomService.join(created.inviteCode(), "guest1");
        JoinRoomResponse guest2 = quizRoomService.join(created.inviteCode(), "guest2");

        QuizRoomResponse room = quizRoomService.getRoom(created.roomId());

        assertThat(guest1.participantId()).isNotEqualTo(guest2.participantId());
        assertThat(room.participants()).hasSize(3);
        assertThat(room.participants())
            .extracting("nickname")
            .containsExactlyInAnyOrder("host", "guest1", "guest2");
        assertThat(room.participants())
            .filteredOn(participant -> !participant.host())
            .allMatch(participant -> participant.online());
    }

    @Test
    @DisplayName("guest refresh can reconnect existing participant without creating duplicate participant")
    void guestReconnectsAfterRefresh() {
        QuizRoomResponse created = quizRoomService.createRoom(
            host.getId(),
            new CreateRoomRequest(quiz.getId(), 5)
        );
        JoinRoomResponse guest = quizRoomService.join(created.inviteCode(), "guest");

        quizRoomService.leave(created.roomId(), guest.participantId());
        QuizRoomResponse offlineRoom = quizRoomService.getRoom(created.roomId());
        assertThat(offlineRoom.participants())
            .filteredOn(participant -> participant.participantId().equals(guest.participantId()))
            .singleElement()
            .satisfies(participant -> assertThat(participant.online()).isFalse());

        QuizRoomResponse reconnectedRoom = quizRoomService.reconnectParticipant(
            created.roomId(),
            guest.participantId()
        );

        assertThat(reconnectedRoom.participants()).hasSize(2);
        assertThat(reconnectedRoom.participants())
            .filteredOn(participant -> participant.participantId().equals(guest.participantId()))
            .singleElement()
            .satisfies(participant -> assertThat(participant.online()).isTrue());
    }

    @Test
    @DisplayName("host refresh can reconnect waiting room after temporary disconnect")
    void hostReconnectsAfterRefresh() {
        QuizRoomResponse created = quizRoomService.createRoom(
            host.getId(),
            new CreateRoomRequest(quiz.getId(), 5)
        );
        QuizRoom room = quizRoomRepository.findById(created.roomId()).orElseThrow();
        Long hostParticipantId = room.getHostParticipant().getId();

        LeaveRoomResult leaveResult = quizRoomService.leaveAndCloseWaitingRoomIfNeeded(
            created.roomId(),
            hostParticipantId
        );
        assertThat(leaveResult.closed()).isTrue();

        QuizRoomResponse reconnectedRoom = quizRoomService.reconnectHost(created.roomId(), host.getId());

        assertThat(reconnectedRoom.participants())
            .filteredOn(participant -> participant.host())
            .singleElement()
            .satisfies(participant -> assertThat(participant.online()).isTrue());
        assertThat(quizRoomRepository.findById(created.roomId())).isPresent();
    }
}
