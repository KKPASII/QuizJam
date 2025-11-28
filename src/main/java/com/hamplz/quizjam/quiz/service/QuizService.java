package com.hamplz.quizjam.quiz.service;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.NotFoundException;
import com.hamplz.quizjam.openai.OpenAiService;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.*;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.AnswerRepository;
import com.hamplz.quizjam.quiz.repository.QuestionRepository;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import com.hamplz.quizjam.util.PdfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final OpenAiService openAiService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public QuizService(OpenAiService openAiService, QuizRepository quizRepository, QuestionRepository questionRepository, AnswerRepository answerRepository, UserRepository userRepository) {
        this.openAiService = openAiService;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    public QuizResponse createQuiz(Long userId, QuizCreateFormat quizCreateFormat, MultipartFile file) throws IOException {
        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // ✅ 1. 파일 유효성 검사 추가 (필수)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF 파일이 비어있거나 업로드되지 않았습니다.");
        }

        // ✅ 2. Content-Type 검사 (선택 사항이지만 추천)
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        File tempFile = File.createTempFile("upload-", ".pdf");
        String pdfText;

        try {
            log.info("📄 PDF 파일 처리 시작: {} (Size: {} bytes)", file.getOriginalFilename(), file.getSize());
            file.transferTo(tempFile);

            // PDF 추출
            pdfText = PdfUtil.extractText(tempFile);

            // 텍스트가 너무 짧으면(추출 실패 등) 에러 처리
            if (pdfText == null || pdfText.trim().isEmpty()) {
                throw new IOException("PDF에서 텍스트를 추출할 수 없습니다. (이미지 위주 파일일 수 있음)");
            }

            log.debug("📝 추출된 텍스트 (앞부분 300자): {}", pdfText.substring(0, Math.min(300, pdfText.length())));

        } catch (Exception e) {
            log.error("❌ PDF 추출 실패: {}", e.getMessage(), e);
            throw new IOException("PDF 처리 실패", e);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("⚠️ 임시 파일 삭제 실패: {}", tempFile.getAbsolutePath());
            } else {
                log.debug("🧹 임시 파일 삭제 완료");
            }
        }

        OpenAiResponse openAiResponse = openAiService.generateQuizFromPdf(userId, pdfText, quizCreateFormat);

        Quiz savedQuiz = saveQuizData(userId, quizCreateFormat, openAiResponse);

        return QuizMapper.toQuizResponse(savedQuiz);
    }

    @Transactional
    public Quiz saveQuizData(Long userId, QuizCreateFormat quizCreateFormat, OpenAiResponse result) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 1️⃣ Quiz 엔티티 생성
        Quiz quiz = Quiz.create(
            user,
            quizCreateFormat.title(),
            quizCreateFormat.type(),
            quizCreateFormat.difficulty(),
            quizCreateFormat.questionCount(),
            quizCreateFormat.timeLimitMin()
        );

        // 2️⃣ 데이터 검증
        if (result.questions().size() != result.answers().size()) {
            log.error("❌ Question과 Answer 개수 불일치: Q={}, A={}",
                result.questions().size(), result.answers().size());
            throw new IllegalStateException("Question과 Answer 개수가 일치하지 않습니다.");
        }

        // 3️⃣ 순서 기반 Question + Answer 매핑
        for (int i = 0; i < result.questions().size(); i++) {
            OpenAiResponse.QuestionForm q = result.questions().get(i);
            OpenAiResponse.AnswerForm a = result.answers().get(i);

            Question question = Question.create(
                quiz,
                q.questionText(),
                q.options(),
                q.hint()
            );

            // Answer 생성 및 연결
            Answer answer = Answer.create(
                question,
                a.correctAnswer(),
                a.explanation()
            );

            question.setAnswer(answer);
            quiz.addQuestion(question);
        }

        // 4️⃣ 저장 (Cascade로 Question, Answer도 함께 저장됨)
        return quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(
            () -> new NotFoundException(ErrorCode.QUIZ_NOT_FOUND)
        );

        return QuizMapper.toQuizResponse(quiz);
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Quiz> quizPage = quizRepository.findAllByUserId(userId, pageable);

        return quizPage.map(QuizMapper::toQuizResponse);
    }

    @Transactional(readOnly = true)
    public List<QuizQuestion> getQuizQuestions(Long quizId) {
        List<Question> questions = questionRepository.findAllByQuizId(quizId);

        return QuizMapper.toQuizQuestionList(questions);
    }

    @Transactional(readOnly = true)
    public List<QuizAnswer> getQuizAnswers(Long quizId) {
        List<Answer> answers = answerRepository.findAllByQuizIdOrderByQuestionId(quizId);

        return QuizMapper.toQuizAnswerList(answers);
    }

    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }
}
