package com.hamplz.quizjam.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.hamplz.quizjam.common.aop.ExecutionTime;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.quiz.FailRequestOpenAiException;
import com.hamplz.quizjam.exception.quiz.InvalidQuizParseException;
import com.hamplz.quizjam.openai.dto.OpenAiRequest;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.service.QuizAsyncService;
import com.hamplz.quizjam.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private final OpenAiClient openAiClient;
    private final QuizAsyncService quizAsyncService;

    @Value("${openai.model}")
    private String model;

    public OpenAiService(OpenAiClient openAiClient, QuizAsyncService quizAsyncService) {
        this.openAiClient = openAiClient;
        this.quizAsyncService = quizAsyncService;
    }

    @Transactional
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

            // 비동기 DB 저장 실행
            quizAsyncService.saveQuizAsync(userId, quizCreateFormat, result);

            return result;

        } catch (Exception e) {
            log.error("❌ OpenAI 응답 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String pdfText, QuizCreateFormat format) {
        String type = format.type().toLowerCase(); // "객관식" | "주관식" | "ox"

        // 🧩 타입별 스키마 구성
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
                  "hint": "힌트 또는 null"
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
                  예: questions[0]의 정답은 answers[0]에 있어야 한다.
                """;
            }

            case "주관식" -> {
                schema = """
            {
              "questions": [
                {
                  "questionText": "문제 내용",
                  "hint": "힌트 또는 null"
                }
              ],
              "answers": [
                {
                  "correctAnswer": "정답 내용 (단답형 또는 문장형)",
                  "explanation": "정답 해설"
                }
              ]
            }
            """;
                extraRule = """
                - `options`는 null로 설정하라.
                - `correctAnswer`는 간결한 정답 텍스트로 작성하라.
                - **questions와 answers의 순서는 반드시 일치해야 한다.**
                """;
            }

            case "ox", "o/x", "ox퀴즈" -> {
                schema = """
            {
              "questions": [
                {
                  "questionText": "문제 내용",
                  "options": { "A": "O", "B": "X" },
                  "hint": "힌트 또는 null"
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
                """;
            }

            default -> {
                schema = "{ \"questions\": [], \"answers\": [] }";
                extraRule = "⚠️ 알 수 없는 퀴즈 유형";
            }
        }

        return """
        너는 대한민국의 **교육용 퀴즈 생성 전문 AI**이다.
        아래 제공된 텍스트와 퀴즈 정보를 바탕으로 학습자가 내용을 복습할 수 있도록 **문제 세트(JSON)**를 생성해야 한다.

        ## 🧾 퀴즈 정보
        - 제목(Title): %s
        - 유형(Type): %s
        - 난이도(Difficulty): %s
        - 문항 수(QuestionCount): %s개
        - 제한 시간(Time): %s초
    
        ## 🎯 목표
        아래 JSON 스키마에 따라 문제(`questions`)와 정답(`answers`)을 **정확히 JSON 형식으로만 출력**하라.
    
        ## ✅ 출력 스키마
        %s
    
        ## ⚙️ 출력 규칙 (아주 중요)
        1. 출력은 반드시 **순수 JSON 한 덩어리**여야 한다. (설명, 코드펜스, 마크다운, 불릿 등 절대 금지)
        2. 출력은 반드시 **"{" 로 시작하고 "}" 로 끝나야 한다.**
        3. `"questions"`와 `"answers"`는 반드시 존재해야 한다.
        4. **모든 문제와 정답 쌍(questions[i], answers[i])이 반드시 1:1로 일치해야 한다.**
        5. **출력이 길어도 끝까지 완전한 JSON을 생성하라.**
          - 만약 토큰 제한(max_tokens)에 근접하면, 해설(`explanation`)만 간략히 요약하라.
          - **절대로 닫힌 JSON을 출력해야 한다.**
        6. 문제의 개수는 문항 수 만큼만 만들어라.
        7. 보기(`options`)는 반드시 4개(A~D)로 구성하라.
        8. 전문 용어(JVM, API, OOP 등)는 그대로 영어로 두되, 설명은 자연스러운 한국어로 작성하라.
        9. 문제 난이도 수준을 그에 맞게 조정하라.
          - `쉬움`: 기초 개념 이해 중심
          - `보통`: 응용과 개념 구분 중심
          - `어려움`: 분석·비판적 사고 중심
        
        ## 🚨 필수 출력 조건
        - 출력이 끝날 때 구조가 불완전하면 파싱 오류가 발생하므로 반드시 마지막 줄이 `}`로 끝나야 한다.
        - %s
        
        ## 📚 참고 텍스트
        아래 내용을 기반으로 문제를 생성하라:
        """.formatted(
            format.title(),
            format.type(),
            format.difficulty(),
            format.questionCount(),
            format.timeLimitMin(),
            schema,
            extraRule
        ) + pdfText;
    }

    @ExecutionTime
    private OpenAiResponse requestOpenAi(String prompt) {

        try {
            String responseJson = openAiClient.sendPromptAsString(
                new OpenAiRequest(
                    model,
                    List.of(new OpenAiRequest.Message("user", prompt)),
                    0.7,
                    1800
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
