package com.hamplz.quizjam.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {
    private static final Logger log = LoggerFactory.getLogger(JsonMapConverter.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("❌ Map -> JSON 변환 실패: {}", e.getMessage());
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            // 1️⃣ 일단 트리 구조로 읽습니다.
            JsonNode node = mapper.readTree(dbData);

            // 2️⃣ [핵심] 만약 "문자열(Text)"로 감싸져 있다면? (이중 직렬화된 데이터)
            // 예: "{\"A\":\"...\"}" -> 껍질을 벗기고 다시 파싱
            if (node.isTextual()) {
                return mapper.readValue(node.asText(), new TypeReference<Map<String, String>>() {});
            }

            // 3️⃣ 정상적인 JSON 객체라면?
            // 예: {"A":"..."} -> 바로 Map으로 변환
            return mapper.convertValue(node, new TypeReference<Map<String, String>>() {});

        } catch (Exception e) {
            log.error("❌ JSON -> Map 변환 실패 (Data: {}): {}", dbData, e.getMessage());
            return null;
        }
    }
}
