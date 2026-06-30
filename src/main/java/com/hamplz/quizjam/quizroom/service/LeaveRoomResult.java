package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;

public record LeaveRoomResult(
    boolean closed,
    Long roomId,
    QuizRoomResponse room
) {
    public static LeaveRoomResult closed(Long roomId) {
        return new LeaveRoomResult(true, roomId, null);
    }

    public static LeaveRoomResult open(QuizRoomResponse room) {
        return new LeaveRoomResult(false, room.roomId(), room);
    }
}
