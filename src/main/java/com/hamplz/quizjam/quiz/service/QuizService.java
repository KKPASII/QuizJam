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
@Transactional
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

    public List<QuizQuestion> createQuiz(Long userId, QuizCreateFormat quizCreateFormat, MultipartFile file) throws IOException {
        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        File tempFile = File.createTempFile("upload-", ".pdf");
        String pdfText;

        try {
            // 1️⃣ PDF 텍스트 추출
            log.info("📄 PDF 텍스트 추출 시작...");
            file.transferTo(tempFile);
            pdfText = PdfUtil.extractText(tempFile);
            log.debug("📝 추출된 텍스트 (앞부분 300자): {}",
                pdfText.substring(0, Math.min(300, pdfText.length())));
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

        return QuizMapper.toQuizQuestionList(openAiResponse);
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
