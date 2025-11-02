package com.hamplz.quizjam.exception;

public class BadRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public BadRequestException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
