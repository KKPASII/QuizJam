package com.hamplz.quizjam.quizroom.dto;

import com.hamplz.quizjam.quizroom.entity.QuizRoom;

import java.util.List;

public record QuizRoomResponse(
    Long roomId,
    Long quizId,
    String inviteCode,
    Long hostUserId,
    List<String> participants
) {
    public static QuizRoomResponse from(QuizRoom room) {
        return new QuizRoomResponse(
            room.getId(),
            room.getQuizId(),
            room.getInviteCode(),
            room.getHostUserId(),
            room.getParticipantList().stream()
                .map(p -> p.getNickname())
                .toList()
        );
    }
}

