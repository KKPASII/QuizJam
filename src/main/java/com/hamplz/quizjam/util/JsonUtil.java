package com.hamplz.quizjam.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /** ```json ... ``` 같은 코드펜스를 제거 */
    public static String stripCodeFences(String content) {
        if (content == null) return null;
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            // 첫 줄 “```json” 또는 “```” 제거
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            // 끝 “```” 제거
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }
}
