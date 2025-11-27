package com.hamplz.quizjam.openai.prompt.factory;

import com.hamplz.quizjam.openai.prompt.strategy.MultipleChoicePromptStrategy;
import com.hamplz.quizjam.openai.prompt.strategy.OxPromptStrategy;
import com.hamplz.quizjam.openai.prompt.strategy.QuizPromptStrategy;
import com.hamplz.quizjam.openai.prompt.strategy.ShortAnswerPromptStrategy;

public class QuizPromptStrategyFactory {
    public static QuizPromptStrategy fromType(String type) {
        return switch (type.toLowerCase()) {
            case "객관식" -> new MultipleChoicePromptStrategy();
            case "단답식" -> new ShortAnswerPromptStrategy();
            case "ox", "o/x", "ox퀴즈" -> new OxPromptStrategy();
            default -> throw new IllegalArgumentException("지원하지 않는 퀴즈 유형: " + type);
        };
    }
}
