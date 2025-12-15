package com.hamplz.quizjam.exception;

public class ConflictException extends RuntimeException {
    private final ErrorCode errorCode;

    public ConflictException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
