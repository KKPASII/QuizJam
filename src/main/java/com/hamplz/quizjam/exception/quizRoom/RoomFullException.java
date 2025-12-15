package com.hamplz.quizjam.exception.quizRoom;

import com.hamplz.quizjam.exception.ErrorCode;

public class RoomFullException extends RuntimeException {
    private final ErrorCode errorCode;

    public RoomFullException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
