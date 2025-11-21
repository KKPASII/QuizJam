package com.hamplz.quizjam.quiz.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hamplz.quizjam.util.JsonUtil.MAPPER;

public class QuizMapper {

    private static final Logger log = LoggerFactory.getLogger(QuizMapper.class);

    private QuizMapper() {}

    public static QuizResponse toQuizResponse(Quiz quiz) {
        return new QuizResponse(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getQuizType(),
            quiz.getDifficulty(),
            quiz.getQuestionCount(),
            quiz.getTimeLimitMin()
        );
    }

    public static QuizQuestion toQuizQuestion(Question question) {
        return new QuizQuestion(
            question.getQuestionText(),
            question.getOptions() != null ? question.getOptions() : Collections.emptyMap(),
            question.getHint()
        );
    }

    public static List<QuizQuestion> toQuizQuestionList(List<Question> questions) {
        return questions.stream()
            .map(QuizMapper::toQuizQuestion)
            .collect(Collectors.toList());
    }

    public static QuizAnswer toQuizAnswer(Answer answer) {
        return new QuizAnswer(
            answer.getCorrectAnswer(),
            answer.getExplanation()
        );
    }

    public static List<QuizAnswer> toQuizAnswerList(List<Answer> answers) {
        return answers.stream()
            .map(QuizMapper::toQuizAnswer)
            .collect(Collectors.toList());
    }

    public static List<QuizQuestion> toQuizQuestionList(OpenAiResponse openAiResponse) {
        return openAiResponse.questions().stream()
            .map(q -> new QuizQuestion(
                q.questionText(),
                q.options(),
                q.hint()
            ))
            .collect(Collectors.toList());
    }
}
