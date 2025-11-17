package com.hamplz.quizjam.quiz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import com.hamplz.quizjam.util.JsonUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizAsyncService {

    private static final Logger log = LoggerFactory.getLogger(QuizAsyncService.class);

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public QuizAsyncService(QuizRepository quizRepository, UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
    }

    @Async
    public void saveQuizAsync(Long userId, QuizCreateFormat quizCreateFormat, OpenAiResponse result) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        try {
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

                // Question 생성
                String optionsJson = (q.options() != null)
                    ? JsonUtil.MAPPER.writeValueAsString(q.options())
                    : null;

                Question question = Question.create(
                    quiz,
                    q.questionText(),
                    optionsJson,
                    q.hint()
                );

                // Answer 생성 및 연결
                Answer answer = Answer.create(
                    question,
                    a.correctAnswer(),
                    a.explanation()
                );
                question.setAnswer(answer);

                // Quiz에 추가
                quiz.addQuestion(question);
            }

            // 4️⃣ 저장 (Cascade로 Question, Answer도 함께 저장됨)
            quizRepository.save(quiz);

            log.info("🗂️ [Async] 퀴즈 및 문제/정답 저장 완료 (Quiz ID: {}, 문제 수: {})",
                quiz.getId(), quiz.getQuestionSize());

        } catch (JsonProcessingException e) {
            log.error("❌ [Async] JSON 변환 중 오류: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ [Async] 퀴즈 저장 실패: {}", e.getMessage(), e);
        }
    }
}
