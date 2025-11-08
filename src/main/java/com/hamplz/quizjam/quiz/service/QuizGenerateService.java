package com.hamplz.quizjam.quiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hamplz.quizjam.openai.OpenAiClient;
import com.hamplz.quizjam.openai.dto.OpenAiRequest;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.repository.AnswerRepository;
import com.hamplz.quizjam.quiz.repository.QuestionRepository;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.util.JsonUtil;
import com.hamplz.quizjam.util.PdfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class QuizGenerateService {

    private static final Logger log = LoggerFactory.getLogger(QuizGenerateService.class);

    private final OpenAiClient openAiClient;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuizGenerateService(OpenAiClient openAiClient, QuizRepository quizRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.openAiClient = openAiClient;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    @Value("${openai.model}")
    private String model;

    public OpenAiResponse generateQuizFromPdf(MultipartFile file, String title, String type, String difficulty) throws IOException {
        File tempFile = File.createTempFile("upload-", ".pdf");

        try {
            // 1️⃣ PDF 텍스트 추출
            log.info("📄 PDF 텍스트 추출 시작...");
            file.transferTo(tempFile);
            String pdfText = PdfUtil.extractText(tempFile);
            log.debug("📝 추출된 텍스트 (앞부분 300자): {}",
                    pdfText.substring(0, Math.min(300, pdfText.length())));

            // 2️⃣ 프롬프트 구성
            String prompt = buildPrompt(pdfText);
            log.info("🤖 OpenAI 요청 준비 완료 (모델: {})", model);
            log.debug("🧩 Prompt 내용:\n{}", prompt);

            // 3️⃣ OpenAI API 호출
            String responseJson = openAiClient.sendPromptAsString(
                    new OpenAiRequest(
                            model,
                            List.of(new OpenAiRequest.Message("user", prompt)),
                            0.7,
                            800
                    )
            );
            log.info("📤 OpenAI 요청 완료, 응답 수신됨");
            log.debug("📥 Raw OpenAI Response:\n{}", responseJson);

            // 4️⃣ 응답 파싱
            log.info("🧩 OpenAI 응답 파싱 중...");
            JsonNode root = JsonUtil.MAPPER.readTree(responseJson);
            String content = root.get("choices").get(0).get("message").get("content").asText();

            // 5️⃣ 코드펜스 제거 및 JSON 정제
            String jsonOnly = JsonUtil.stripCodeFences(content);

            // ✅ 보기 좋게 Pretty Print로 콘솔 출력
            try {
                String pretty = JsonUtil.MAPPER.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(JsonUtil.MAPPER.readTree(jsonOnly));
                log.info("✅ 정제된 JSON 응답 (Pretty Print):\n{}", pretty);
            } catch (Exception ignore) {
                log.warn("⚠️ Pretty Print 변환 실패 — raw JSON 그대로 사용");
            }

            // 6️⃣ JSON → DTO 변환
            OpenAiResponse result = JsonUtil.MAPPER.readValue(jsonOnly, OpenAiResponse.class);
            log.info("🎯 퀴즈 생성 성공 (문항 수: {}, 정답 수: {})",
                    result.questions().size(), result.answers().size());

            return result;

        } catch (Exception e) {
            log.error("❌ OpenAI 응답 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);

        } finally {
            // 7️⃣ 임시 파일 삭제
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("⚠️ 임시 파일 삭제 실패: {}", tempFile.getAbsolutePath());
            } else {
                log.debug("🧹 임시 파일 삭제 완료");
            }
        }
    }


    private String buildPrompt(String pdfText) {
        return """
        너는 주어진 텍스트를 분석해서 **객관식 문제 세트(JSON)**를 생성하는 교육을 위한 AI이다.
        너는 주어진 텍스트 내용을 기반으로 객관식 문제 세트를 만든다.
        반드시 아래 JSON 스키마 '그대로'만 출력해:
        {
          "questions": [
            {
              "id": 1,
              "quiz_id": 1,
              "question_text": "문제 내용",
              "options": { "A": "...", "B": "...", "C": "...", "D": "..." },
              "hint": "힌트 또는 null"
            }
          ],
          "answers": [
            {
              "id": 1,
              "question_id": 1,
              "correct_answer": "A|B|C|D",
              "explanation": "정답 해설"
            }
          ]
        }
        - 반드시 JSON만 출력(코드펜스 금지)
        - questions[].id 와 answers[].id / question_id 간 일관성을 유지
        - quiz_id는 주어진 숫자 고정임

        텍스트:
        """ + pdfText;
    }

    private OpenAiResponse requestOpenAi(String pdfText, String prompt) {
        // 3️⃣ OpenAI API 호출
        String responseJson = openAiClient.sendPromptAsString(
                new OpenAiRequest(
                        model,
                        List.of(new OpenAiRequest.Message("user", prompt)),
                        0.7,
                        800
                )
        );

        try {
            JsonNode root = JsonUtil.MAPPER.readTree(responseJson);
            String content = root.get("choices").get(0).get("message").get("content").asText();
            String cleanJson = JsonUtil.stripCodeFences(content);
            return JsonUtil.MAPPER.readValue(cleanJson, OpenAiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패", e);
        }
    }
}
