package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QuizRoomSerivce {
    private static final Logger log = LoggerFactory.getLogger(QuizRoomSerivce.class);

    public QuizRoomResponse createRoom(Long userId, Long quizId) {

    }
    public QuizRoomResponse getRoom(Long roomId) {

    }
    public QuizRoomResponse updateStatus(Long roomId, boolean start) {

    }

    public void deleteRoom(Long roomId) {

    }
}
