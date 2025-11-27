package com.hamplz.quizjam.openai.prompt.strategy;

public class OxPromptStrategy implements QuizPromptStrategy {
    @Override
    public String schema() {
        return """
        {
          "questions": [
            {
              "questionText": "문제 내용",
              "options": { "A": "O", "B": "X" },
              "hint": "참/거짓 판단에 필요한 개념 설명 또는 null"
            }
          ],
          "answers": [
            {
              "correctAnswer": "A|B",
              "explanation": "정답 해설"
            }
          ]
        }
        """;
    }

    @Override
    public String extraRule() {
        return """
        - options는 반드시 {"A": "O", "B": "X"}로 고정하라.
        - `correctAnswer`는 "A"(O) 또는 "B"(X) 중 하나여야 한다.
        - 대부분의 OX 문제는 힌트가 필요 없으므로 `hint`는 null로 설정하는 것이 기본이다.
        - 단, 헷갈릴 수 있는 개념이 있을 때만 매우 간단한 힌트를 한 문장으로 작성하라.
        - 힌트는 정답을 직접적으로 드러내거나 암시하지 않는다.
        """;
    }
}
