package com.hamplz.quizjam.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach((error) -> {
                    if (error instanceof FieldError fe) {
                        errors.put(fe.getField(), fe.getDefaultMessage());
                    }
                });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParsingException(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getReason());
        return new ResponseEntity<>(errors, ex.getStatusCode());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(error);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnAuthorizedException(UnAuthorizedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getErrorCode().getMessage());
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUnexpectedException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 🌟 추가: OpenAI API 호출 시 발생하는 HTTP 에러 처리 (400, 401, 429 등)
     * 특히 토큰 제한(context_length_exceeded) 에러를 잡아서 안내합니다.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException ex) {
        Map<String, String> error = new HashMap<>();
        String responseBody = ex.getResponseBodyAsString();

        // 1. 토큰 제한(용량 초과) 에러인지 확인
        if (responseBody.contains("context_length_exceeded")) {
            error.put("error", "파일 내용이 너무 깁니다. (PDF 페이지를 줄이거나 텍스트 위주 파일을 사용해주세요)");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // 400 에러 반환
        }

        // 2. 그 외의 OpenAI 에러 (API 키 만료 등)
        error.put("error", "AI 서비스 요청 중 오류가 발생했습니다: " + ex.getStatusText());
        return new ResponseEntity<>(error, ex.getStatusCode());
    }
}