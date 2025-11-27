package com.hamplz.quizjam.openai.prompt.strategy;

public class ShortAnswerPromptStrategy implements QuizPromptStrategy {
    @Override
    public String schema() {
        return """
        {
          "questions": [
            {
              "questionText": "문제 내용",
              "hint": "정답을 유추할 수 있는 핵심 단어 초성이나 설명"
            }
          ],
          "answers": [
            {
              "correctAnswer": "정답 내용 (단답형)",
              "explanation": "정답 해설"
            }
          ]
        }
        """;
    }

    @Override
    public String extraRule() {
        return """
        - `correctAnswer`는 한 단어 또는 짧은 구로 이루어진 단답형이어야 한다.
        - 문제 문장은 반드시 **단답형을 자연스럽게 유도하는 형태**로 작성하라.
          예시: "~의 명칭은 무엇인가?", "~을 무엇이라고 하는가?", "~의 정의는?", "~을 나타내는 용어는?"
        - "장점은 무엇인가?", "의미는 무엇인가?"처럼 길거나 서술형 답변을 유도하는 질문은 금지한다.
        - `questionText`는 반드시 단답형 정답이 어색하지 않은 문장으로 작성해야 한다.
        - `hint`는 정답을 직접적으로 제공하지 말고 간접적인 연관 키워드를 한 문장으로 제시하라.
        """;
    }
}
