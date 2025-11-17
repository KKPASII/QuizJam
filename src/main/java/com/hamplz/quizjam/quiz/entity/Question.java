package com.hamplz.quizjam.quiz.entity;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    private String questionText;

    @Column(columnDefinition = "json")
    private String options;  // 보기 {"A": "사과", "B": "바나나"}

    private String hint;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;

    protected Question() {}

    private Question(Quiz quiz, String questionText, String options, String hint) {
        this.quiz = quiz;
        this.questionText = questionText;
        this.options = options;
        this.hint = hint;
    }

    public static Question create(Quiz quiz, String questionText, String options, String hint) {
        return new Question(quiz, questionText, options, hint);
    }

    public Quiz getQuiz() {
        return this.quiz;
    }

    public String getQuestionText() {
        return this.questionText;
    }

    public String getOptions() {
        return this.options;
    }

    public String getHint() {
        return this.hint;
    }

    public Answer getAnswer() {
        return this.answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}