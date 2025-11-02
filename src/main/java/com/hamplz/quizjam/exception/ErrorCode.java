package com.hamplz.quizjam.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "존재하지 않는 사용자입니다."),

    FORBIDDEN_USER(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_EXIST(HttpStatus.NOT_FOUND, "요청한 사용자를 찾을 수 없습니다."), // 일반 조회 시
    INVALID_USER_INFO(HttpStatus.UNAUTHORIZED, "유효하지 않은 유저 정보입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
