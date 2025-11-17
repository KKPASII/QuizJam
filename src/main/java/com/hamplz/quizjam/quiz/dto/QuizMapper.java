package com.hamplz.quizjam.quiz.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hamplz.quizjam.openai.dto.OpenAiResponse;
import com.hamplz.quizjam.quiz.entity.Answer;
import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.entity.Quiz;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hamplz.quizjam.util.JsonUtil.MAPPER;

public class QuizMapper {

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
            question.getOptions(),
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
                mapToJson(q.options()),
                q.hint()
            ))
            .collect(Collectors.toList());
    }

    private static String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("옵션 직렬화 실패", e);
        }
    }
}
