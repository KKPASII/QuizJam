package com.hamplz.quizjam.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.hamplz.quizjam.common.aop.ExecutionTime;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.quiz.FailRequestOpenAiException;
import com.hamplz.quizjam.exception.quiz.InvalidQuizParseException;
import com.hamplz.quizjam.openai.dto.OpenAiRequest;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.openai.prompt.PromptTemplate;
import com.hamplz.quizjam.openai.prompt.factory.QuizPromptStrategyFactory;
import com.hamplz.quizjam.openai.prompt.strategy.QuizPromptStrategy;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.util.JsonUtil;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

    private static final int MAX_TOKEN_LIMIT = 120000;

    private final OpenAiClient openAiClient;

    @Value("${openai.model}")
    private String model;

    public OpenAiService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public OpenAiResponse generateQuizFromPdf(Long userId, String pdfText, QuizCreateFormat quizCreateFormat) {

        try {
            // 프롬프트 구성
            String prompt = buildPrompt(pdfText, quizCreateFormat);

            log.info("🤖 OpenAI 요청 준비 완료 (모델: {})", model);
            log.debug("🧩 Prompt 내용:\n{}", prompt);

            // OpenAI API 호출
            OpenAiResponse result = requestOpenAi(prompt);

            log.info("🎯 퀴즈 생성 성공 (문항 수: {}, 정답 수: {})",
                result.questions().size(), result.answers().size());

            return result;

        } catch (Exception e) {
            log.error("❌ OpenAI 응답 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String pdfText, QuizCreateFormat format) {
        String type = format.type().toLowerCase(); // "객관식" | "단답식" | "ox"

        QuizPromptStrategy quizPromptStrategy = QuizPromptStrategyFactory.fromType(type);
        String schema = quizPromptStrategy.schema();
        String extraRule = quizPromptStrategy.extraRule();

        // 1️⃣ 템플릿(지시사항) 먼저 생성 (PDF 내용 제외)
        String instructionTemplate = PromptTemplate.build(format, schema, extraRule);

        // 2️⃣ 토큰 계산 및 자르기 (핵심 로직) 🔥
        String finalPdfText = truncatePdfTextIfNeeded(pdfText, instructionTemplate);

        // 3️⃣ 최종 결합
        return instructionTemplate + "\n\n" + finalPdfText;
    }

    /**
     * ✂️ 토큰 제한에 맞춰 PDF 텍스트를 자르는 메서드
     */
    private String truncatePdfTextIfNeeded(String pdfText, String instructionTemplate) {
        // GPT-4, GPT-4o, GPT-3.5-Turbo 등 최신 모델은 CL100K_BASE 인코딩 사용
        Encoding encoding = registry.getEncoding(EncodingType.CL100K_BASE);

        int instructionTokens = encoding.countTokens(instructionTemplate);
        int pdfTokens = encoding.countTokens(pdfText);

        // 남은 공간 계산 (전체 제한 - 지시사항 토큰 - 여유분 500)
        int availableForPdf = MAX_TOKEN_LIMIT - instructionTokens - 500;

        log.info("📊 토큰 분석 - 지시사항: {}, PDF원본: {}, 사용가능: {}",
            instructionTokens, pdfTokens, availableForPdf);

        // 자를 필요가 없으면 원본 반환
        if (pdfTokens <= availableForPdf) {
            return pdfText;
        }

        // 자르기 로직 수행
        log.warn("⚠️ PDF 내용이 너무 길어 잘라냅니다. ({} -> {} tokens)", pdfTokens, availableForPdf);

        // 방법: 글자수 비율로 안전하게 자르기 (Jtokkit으로 정확히 자르는 건 오버헤드가 큼)
        double ratio = (double) availableForPdf / pdfTokens;
        int cutIndex = (int) (pdfText.length() * ratio);

        // 95% 지점에서 자름 (안전 마진)
        return pdfText.substring(0, (int)(cutIndex * 0.95)) + "\n...[내용이 너무 길어 생략됨]";
    }

    @ExecutionTime
    private OpenAiResponse requestOpenAi(String prompt) {

        try {
            String responseJson = openAiClient.sendPromptAsString(
                new OpenAiRequest(
                    model,
                    List.of(new OpenAiRequest.Message("user", prompt)),
                    0.7,
                    4096,
                    new OpenAiRequest.ResponseFormat("json_object")
                )
            );
            log.info("📤 OpenAI 요청 완료, 응답 수신됨");
            log.debug("📥 Raw OpenAI Response:\n{}", responseJson);

            // JSON 파싱
            log.info("🧩 OpenAI 응답 파싱 중...");
            JsonNode root = JsonUtil.MAPPER.readTree(responseJson);
            String content = root.get("choices").get(0).get("message").get("content").asText();
            String cleanJson = JsonUtil.stripCodeFences(content); // 🧹 코드펜스 / 개행 / 이스케이프 / 따옴표 언랩 모두 정리

            log.debug("🧩 정제된 JSON Content:\n{}", cleanJson);

            return JsonUtil.MAPPER.readValue(cleanJson, OpenAiResponse.class);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("❌ OpenAI 응답 JSON 파싱 실패: {}", e.getMessage());
            throw new InvalidQuizParseException(ErrorCode.INVALID_QUIZ_PARSE);
        } catch (Exception e) {
            log.error("❌ OpenAI API 호출 실패: {}", e.getMessage());
            throw new FailRequestOpenAiException(ErrorCode.FAIL_REQUEST_OPENAI);
        }
    }
}
