package com.hamplz.quizjam.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.hamplz.quizjam.common.aop.ExecutionTime;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.quiz.FailRequestOpenAiException;
import com.hamplz.quizjam.exception.quiz.InvalidQuizParseException;
import com.hamplz.quizjam.openai.dto.OpenAiRequest;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
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
        String type = format.type().toLowerCase(); // "객관식" | "주관식" | "ox"

        String schema;
        String extraRule;

        switch (type) {
            case "객관식" -> {
                schema = """
            {
              "questions": [
                {
                  "questionText": "문제 내용",
                  "options": { "A": "보기 A", "B": "보기 B", "C": "보기 C", "D": "보기 D" },
                  "hint": "문제 풀이에 도움이 되는 핵심 힌트 (최대 1문장)"
                }
              ],
              "answers": [
                {
                  "correctAnswer": "A|B|C|D",
                  "explanation": "정답 해설"
                }
              ]
            }
            """;
                extraRule = """
                - 보기(`options`)는 반드시 4개(A~D)로 구성하라.
                - `correctAnswer`는 "A", "B", "C", "D" 중 하나여야 한다.
                - **questions와 answers의 순서는 반드시 일치해야 한다.**
                - **모든 문제에 대해 반드시 유용한 힌트를 작성하라.**
                """;
            }

            case "주관식" -> {
                schema = """
            {
              "questions": [
                {
                  "questionText": "문제 내용",
                  "hint": "정답을 유추할 수 있는 핵심 단어 초성이나 설명"
                }
              ],
              "answers": [
                {
                  "correctAnswer": "정답 내용 (단답형)",
                  "explanation": "정답 해설"
                }
              ]
            }
            """;
                extraRule = """
                - `options`는 null로 설정하라.
                - `correctAnswer`는 간결한 정답 텍스트로 작성하라.
                - **questions와 answers의 순서는 반드시 일치해야 한다.**
                - **모든 문제에 대해 정답을 유추할 수 있는 힌트를 반드시 포함하라.**
                """;
            }

            case "ox", "o/x", "ox퀴즈" -> {
                schema = """
            {
              "questions": [
                {
                  "questionText": "문제 내용",
                  "options": { "A": "O", "B": "X" },
                  "hint": "참/거짓을 판단하는 데 도움이 되는 개념 설명"
                }
              ],
              "answers": [
                {
                  "correctAnswer": "A|B",
                  "explanation": "정답 해설"
                }
              ]
            }
            """;
                extraRule = """
                - `options`는 반드시 {"A": "O", "B": "X"} 순서로 생성하라.
                - `correctAnswer`는 "A"(O) 또는 "B"(X) 중 하나여야 한다.
                - **questions와 answers의 순서는 반드시 일치해야 한다.**
                - **헷갈리기 쉬운 부분에 대한 힌트를 반드시 제공하라.**
                """;
            }

            default -> {
                schema = "{ \"questions\": [], \"answers\": [] }";
                extraRule = "⚠️ 알 수 없는 퀴즈 유형";
            }
        }

        // 1️⃣ 템플릿(지시사항) 먼저 생성 (PDF 내용 제외)
        String instructionTemplate = """
        너는 대한민국의 **교육용 퀴즈 생성 전문 AI**이다.
        아래 제공된 텍스트와 퀴즈 정보를 바탕으로 학습자가 내용을 복습할 수 있도록 **문제 세트(JSON)**를 생성해야 한다.
            
        ## 🧾 퀴즈 정보
        - 제목: %s
        - 유형: %s
        - 난이도: %s
        - 문항 수: %s개
        - 제한 시간: %s분
                    
        ## 🎯 목표
        아래 JSON 스키마에 따라 문제(`questions`)와 정답(`answers`)을 **정확히 JSON 형식으로만 출력**하라.
                    
        ## ✅ 출력 스키마
        %s
                    
        ## ⚙️ 출력 규칙
        1. 출력은 반드시 **순수 JSON 한 덩어리**여야 한다.
        2. `"questions"`와 `"answers"`의 개수와 순서는 1:1로 일치해야 한다.
        3. **출력이 길어도 끝까지 완전한 JSON을 생성하라.** (중요)
        4. 문제 난이도: %s
        5. **힌트(hint) 필드는 학습자에게 도움이 되는 단서를 작성하라.** (매우 중요)
        
        ## 🚨 필수 조건
        - %s
        
        ## 📚 참고 텍스트
        아래 내용을 기반으로 문제를 생성하라 (내용이 너무 길면 일부 생략될 수 있음):
        """.formatted(
            format.title(),
            format.type(),
            format.difficulty(),
            format.questionCount(),
            format.timeLimitMin(),
            schema,
            format.difficulty(),
            extraRule
        );

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
