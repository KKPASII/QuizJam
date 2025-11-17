package com.hamplz.quizjam.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 인증
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "[Auth] 유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "[Auth] 토큰이 만료되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "[Auth] 토큰이 존재하지 않습니다."),
    FORBIDDEN_USER(HttpStatus.FORBIDDEN, "[Auth] 접근 권한이 없습니다."),

    // 유저
    USER_NOT_EXIST(HttpStatus.NOT_FOUND, "[User] 요청한 사용자를 찾을 수 없습니다."), // 일반 조회 시
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "[User] 요청한 사용자를 찾을 수 없습니다."),
    INVALID_USER_INFO(HttpStatus.UNAUTHORIZED, "[User] 유효하지 않은 사용자 정보입니다."),

    // 퀴즈
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "[Quiz] 퀴즈를 찾지 못했습니다."),
    INVALID_QUIZ_PARSE(HttpStatus.INTERNAL_SERVER_ERROR, "[Quiz] JSON 파싱에 실패했습니다."),
    INVALID_FILE(HttpStatus.BAD_REQUEST, "[File] PDF 읽기 실패"),
    FAIL_REQUEST_OPENAI(HttpStatus.BAD_GATEWAY, "[OpenAI] API 요청 실패"),
    INCORRECT_QUIZ_DATA(HttpStatus.CONFLICT, "[Quiz] Q/A 개수 불일치");


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
