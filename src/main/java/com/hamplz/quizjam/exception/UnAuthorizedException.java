package com.hamplz.quizjam.exception;

public class UnAuthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnAuthorizedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
