package com.hamplz.quizjam.exception.quiz;

import com.hamplz.quizjam.exception.ErrorCode;

public class InvalidQuizParseException extends RuntimeException {
  private final ErrorCode errorCode;

  public InvalidQuizParseException(final ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return this.errorCode;
  }
}
