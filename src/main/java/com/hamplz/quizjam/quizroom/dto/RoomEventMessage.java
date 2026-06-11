package com.hamplz.quizjam.quizroom.dto;

public record RoomEventMessage(
    String type,
    Object payload
) {
    public static RoomEventMessage of(String type, Object payload) {
        return new RoomEventMessage(type, payload);
    }
}
