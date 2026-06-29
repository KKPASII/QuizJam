package com.hamplz.quizjam.quizroom.dto;

import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.entity.QuizRoomStatus;
import com.hamplz.quizjam.quiz.entity.Quiz;

import java.util.List;

public record QuizRoomResponse(
    Long roomId,
    Long quizId,
    String quizTitle,
    int questionCount,
    String inviteCode,
    String invitePath,
    Long hostUserId,
    QuizRoomStatus status,
    int questionTimeLimitSeconds,
    List<ParticipantResponse> participants
) {
    public static QuizRoomResponse from(QuizRoom room, Quiz quiz) {
        return new QuizRoomResponse(
            room.getId(),
            room.getQuizId(),
            quiz.getTitle(),
            quiz.getQuestionCount(),
            room.getInviteCode(),
            "/rooms/join/" + room.getInviteCode(),
            room.getHostUserId(),
            room.getStatus(),
            room.getQuestionTimeLimitSeconds(),
            room.getParticipants().stream()
                .map(ParticipantResponse::from)
                .toList()
        );
    }
}
