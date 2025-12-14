package com.hamplz.quizjam.quiz.service;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.NotFoundException;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.AnswerRepository;
import com.hamplz.quizjam.quiz.repository.QuestionRepository;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuizQueryService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuizQueryService(QuizRepository quizRepository, QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public Quiz getQuiz(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.QUIZ_NOT_FOUND));
    }

    public Page<Quiz> getQuizzes(Long userId, Pageable pageable) {
        return quizRepository.findAllByUserId(userId, pageable);
    }

    public List<Question> getQuizQuestions(Long quizId) {
        return questionRepository.findAllByQuizId(quizId);
    }

    public List<Answer> getQuizAnswers(Long quizId) {
        return answerRepository.findAllByQuizIdOrderByQuestionId(quizId);
    }
}
