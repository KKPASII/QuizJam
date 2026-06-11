package com.hamplz.quizjam.quizroom.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ParticipantSessionRegistry {

    private final Map<String, Long> sessionToRoom = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToParticipant = new ConcurrentHashMap<>();

    public void register(String sessionId, Long roomId, Long participantId) {
        sessionToRoom.put(sessionId, roomId);
        sessionToParticipant.put(sessionId, participantId);
    }

    public Long getRoomId(String sessionId) {
        return sessionToRoom.get(sessionId);
    }

    public Long getParticipantId(String sessionId) {
        return sessionToParticipant.get(sessionId);
    }

    public void remove(String sessionId) {
        sessionToRoom.remove(sessionId);
        sessionToParticipant.remove(sessionId);
    }
}
