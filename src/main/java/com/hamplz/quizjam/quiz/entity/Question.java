package com.hamplz.quizjam.quiz.entity;

import com.hamplz.quizjam.util.JsonMapConverter;
import jakarta.persistence.*;

import java.util.Map;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    private String questionText;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "json") // MySQL이라면 json, 아니라면 text나 clob 권장
    private Map<String, String> options;

    private String hint;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;

    protected Question() {}

    private Question(Quiz quiz, String questionText, Map<String, String> options, String hint) {
        this.quiz = quiz;
        this.questionText = questionText;
        this.options = options;
        this.hint = hint;
    }

    public static Question create(Quiz quiz, String questionText, Map<String, String> options, String hint) {
        return new Question(quiz, questionText, options, hint);
    }

    public Quiz getQuiz() {
        return this.quiz;
    }

    public String getQuestionText() {
        return this.questionText;
    }

    public Map<String, String> getOptions() {
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