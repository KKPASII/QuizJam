package com.hamplz.quizjam.openai.prompt;

import com.hamplz.quizjam.quiz.dto.QuizCreateFormat;

public class PromptTemplate {
    public static String build(
        QuizCreateFormat format,
        String schema,
        String extraRule
    ) {
        return """
        너는 대한민국의 교육용 퀴즈 생성 전문 AI이다.
        아래 제공된 텍스트와 퀴즈 정보를 바탕으로 문제 세트를 JSON으로 생성하라.

        ## 퀴즈 정보
        - 제목: %s
        - 유형: %s
        - 난이도: %s
        - 문항 수: %s개
        - 제한 시간: %s분

        ## 출력 스키마
        %s

        ## 규칙
        1. 출력은 반드시 순수 JSON 한 덩어리여야 한다.
        2. `questions`와 `answers`의 개수, 순서는 1:1로 일치해야 한다.
        3. 힌트는 항상 학습자에게 도움이 되도록 작성한다.
        4. 난이도: %s

        ## 유형별 추가 규칙
        %s

        ## 참고 텍스트
        (너무 길면 일부 생략 가능)
        """.formatted(
            format.title(),
            format.type(),
            format.difficulty(),
            format.questionCount(),
            format.timeLimitMin(),
            schema,
            format.difficulty(),
            extraRule
        );
    }
}
