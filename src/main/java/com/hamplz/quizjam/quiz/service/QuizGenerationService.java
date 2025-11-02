package com.hamplz.quizjam.quiz.service;

import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.util.PdfUtil;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
@Transactional
public class QuizGenerationService {
    private final OpenAiService openAiService;
    private final QuizRepository quizRepository;

    public QuizGenerationService(OpenAiService openAiService, QuizRepository quizRepository) {
        this.openAiService = openAiService;
        this.quizRepository = quizRepository;
    }

    public Quiz generateQuizFromPdf(File pdfFile, QuizCreateFormat quizCreateFormat) throws Exception {
        // 1️⃣ PDF 텍스트 추출
        String text = PdfUtil.extractText(pdfFile);

        // 2️⃣ OpenAI에 문제 요청
        String prompt = """
                You are an AI quiz generator.
                Based on the following text, create 3 quiz questions.
                Each question should include:
                - question text
                - 4 multiple choice options (A,B,C,D)
                - correct answer letter (A/B/C/D)
                - short explanation

                Return JSON array like:
                [
                  {"question": "...", "options": {"A":"...","B":"...","C":"...","D":"..."}, "answer":"B", "explanation":"..."}
                ]

                Text:
                """ + text;

        var request = CompletionRequest.builder()
            .model("gpt-4o-mini")
            .prompt(prompt)
            .maxTokens(800)
            .temperature(0.7)
            .build();

        String result = openAiService.createCompletion(request)
            .getChoices()
            .get(0)
            .getText()
            .trim();

        // 3️⃣ JSON 파싱
        List<Question> questions = parseQuestions(result);

        // 4️⃣ Quiz 엔티티 구성
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuizType("객관식");
        quiz.setDifficulty("Easy");
        quiz.setQuestionCount(questions.size());

        for (Question q : questions) {
            q.setQuiz(quiz);
        }

        quiz.getQuestions().addAll(questions);

        // 5️⃣ 저장
        return quizRepository.save(quiz);
    }

    private List<Question> parseQuestions(String jsonText) {
        // Jackson ObjectMapper 사용
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<QuestionDTO> dtos = mapper.readValue(jsonText,
                mapper.getTypeFactory().constructCollectionType(List.class, QuestionDTO.class));

            List<Question> questions = new ArrayList<>();
            for (QuestionDTO dto : dtos) {
                Question q = new Question();
                q.setQuestionText(dto.question());
                q.setOptions(mapper.writeValueAsString(dto.options()));
                q.setHint(null);

                Answer a = new Answer();
                a.setCorrectAnswer(dto.answer());
                a.setExplanation(dto.explanation());
                a.setQuestion(q);

                q.setAnswer(a);
                questions.add(q);
            }
            return questions;

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to parse OpenAI response: " + e.getMessage(), e);
        }
    }
}
