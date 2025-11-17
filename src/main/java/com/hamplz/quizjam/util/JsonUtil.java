package com.hamplz.quizjam.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * LLM 응답 문자열을 안전한 JSON으로 정제한다.
     * - ```json ... ``` 코드펜스 제거
     * - 불필요한 개행, 탭, 공백 정리
     * - 문자열 JSON 언랩 (ex. "{\"questions\":...}" → {"questions":...})
     * - 백슬래시 이스케이프 제거
     * - 마크다운 헤더 / 텍스트 설명 제거 ("## ✨", "다음은", 등)
     * - 순수 JSON 본문({ "questions": ...)만 추출
     */
    public static String stripCodeFences(String content) {
        if (content == null) return null;

        String trimmed = content.trim();

        // ✅ 1️⃣ 코드펜스 제거
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }

        // ✅ 2️⃣ 불필요한 개행, 탭 제거
        trimmed = trimmed
            .replaceAll("\\r", " ")
            .replaceAll("\\n", " ")
            .replaceAll("\\t", " ")
            .replaceAll("\\s{2,}", " ")
            .trim();

        // ✅ 3️⃣ 문자열 내부 JSON 처리 (예: "{\"questions\":...}" → {"questions":...})
        if (trimmed.startsWith("\"{") && trimmed.endsWith("}\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }

        // ✅ 4️⃣ 백슬래시 이스케이프 제거 (\" → ")
        trimmed = trimmed.replace("\\\"", "\"");

        // ✅ 5️⃣ 마크다운 헤더나 텍스트 제거, 순수 JSON 본문 추출
        // 예: "## ✨ 문제 세트(JSON) { "questions": [...] }" → "{ "questions": [...] }"
        trimmed = trimmed.replaceAll("(?s)^.*?(\\{\\s*\"questions\")", "$1").trim();

        return trimmed;
    }
}