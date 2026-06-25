package com.hamplz.quizjam.quizroom;

import com.hamplz.quizjam.exception.ConflictException;
import com.hamplz.quizjam.exception.ForbiddenException;
import com.hamplz.quizjam.quizroom.entity.Participant;
import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.entity.QuizRoomStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizRoomTest {

    @Test
    @DisplayName("room creation registers host as first participant")
    void createRoomWithHost() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);

        assertThat(room.getQuizId()).isEqualTo(1L);
        assertThat(room.getHostUserId()).isEqualTo(10L);
        assertThat(room.getStatus()).isEqualTo(QuizRoomStatus.WAITING);
        assertThat(room.getQuestionTimeLimitSeconds()).isEqualTo(20);
        assertThat(room.getParticipants()).hasSize(1);
        assertThat(room.getParticipants().get(0).isHost()).isTrue();
    }

    @Test
    @DisplayName("anonymous participant can join waiting room")
    void joinAnonymousParticipant() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);

        Participant participant = room.joinAnonymous("guest");

        assertThat(participant.isAnonymous()).isTrue();
        assertThat(participant.getNickname()).isEqualTo("guest");
        assertThat(room.getParticipants()).hasSize(2);
    }

    @Test
    @DisplayName("nickname must be unique in same room")
    void rejectDuplicateNickname() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);
        room.joinAnonymous("guest");

        assertThatThrownBy(() -> room.joinAnonymous("GUEST"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("only host can start room")
    void rejectStartByNonHost() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);

        assertThatThrownBy(() -> room.start(99L))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("started room rejects new participants")
    void rejectJoinAfterStart() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);
        room.start(10L);

        assertThatThrownBy(() -> room.joinAnonymous("guest"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("leaving participant is marked offline")
    void markParticipantOfflineWhenLeave() {
        QuizRoom room = QuizRoom.create(1L, 10L, "host", "ABCDEFGH", 20);
        Participant participant = room.joinAnonymous("guest");
        ReflectionTestUtils.setField(participant, "id", 20L);

        room.leaveParticipant(participant.getId());

        assertThat(participant.isOnline()).isFalse();
    }
}
