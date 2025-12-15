package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.exception.ConflictException;
import com.hamplz.quizjam.exception.ErrorCode;

public enum QuizRoomStatus {
    WAITING {
        @Override
        public QuizRoomStatus start() {
            return IN_PROGRESS;
        }
    },
    IN_PROGRESS {
        @Override
        public QuizRoomStatus finish() {
            return FINISHED;
        }
    },
    FINISHED;

    public QuizRoomStatus start() {
        throw new ConflictException(ErrorCode.QUIZ_ROOM_ALREADY_STARTED);
    }

    public QuizRoomStatus finish() {
        throw new ConflictException(ErrorCode.QUIZ_ROOM_NOT_IN_PROGRESS);
    }
}