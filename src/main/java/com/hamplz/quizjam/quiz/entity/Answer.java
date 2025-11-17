package com.hamplz.quizjam.quiz.entity;

import jakarta.persistence.*;

@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private String correctAnswer;

    private String explanation;

    protected Answer() {}

    private Answer(Question question, String correctAnswer, String explanation) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public static Answer create(Question question, String correctAnswer, String explanation) {
        return new Answer(question, correctAnswer, explanation);
    }

    public String getCorrectAnswer() {
        return this.correctAnswer;
    }

    public String getExplanation() {
        return this.explanation;
    }
}
