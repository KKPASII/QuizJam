package com.hamplz.quizjam.quizroom.dto;

import com.hamplz.quizjam.quizroom.entity.Participant;

public record ParticipantResponse(
    Long participantId,
    Long userId,
    String nickname,
    boolean host,
    int score
) {
    public static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(
            participant.getId(),
            participant.getUserId(),
            participant.getNickname(),
            participant.isHost(),
            participant.getScore()
        );
    }
}
