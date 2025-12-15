package com.hamplz.quizjam.exception;

public class ForbiddenException extends RuntimeException {
  private final ErrorCode errorCode;

  public ForbiddenException(final ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return this.errorCode;
  }
}
