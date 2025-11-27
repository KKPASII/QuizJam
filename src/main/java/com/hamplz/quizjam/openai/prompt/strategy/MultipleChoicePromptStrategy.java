package com.hamplz.quizjam.openai.prompt.strategy;

public class MultipleChoicePromptStrategy implements QuizPromptStrategy {

    @Override
    public String schema() {
        return """
        {
          "questions": [
            {
              "questionText": "문제 내용",
              "options": { "A": "보기 A", "B": "보기 B", "C": "보기 C", "D": "보기 D" },
              "hint": "문제 풀이에 도움이 되는 핵심 힌트 (최대 1문장)"
            }
          ],
          "answers": [
            {
              "correctAnswer": "A|B|C|D",
              "explanation": "정답 해설"
            }
          ]
        }
        """;
    }

    @Override
    public String extraRule() {
        return """
        - 보기(options)는 반드시 4개(A~D)로 구성하라.
        - correctAnswer는 "A", "B", "C", "D" 중 하나여야 한다.
        - **모든 문제에 대해 반드시 유용한 힌트를 작성하라.**
        - `hint`는 정답을 직접적으로 제공하지 말고 간접적인 한 문장으로 제시하라.
        """;
    }
}
