package com.hamplz.quizjam.quiz.entity;

import com.hamplz.quizjam.user.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    private String title;

    private String quizType;

    private String difficulty;

    private int questionCount;

    private Long timeLimitMin;

    protected Quiz() {}

    private Quiz(User user, String title, String quizType, String difficulty, int questionCount, long timeLimitMin) {
        this.user = user;
        this.title = title;
        this.quizType = quizType;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.timeLimitMin = timeLimitMin;
    }

    public static Quiz create(User user, String title, String quizType, String difficulty, int questionCount, long timeLimitMin) {
        return new Quiz(user, title, quizType, difficulty, questionCount, timeLimitMin);
    }

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public String getTitle() {
        return this.title;
    }

    public String getQuizType() {
        return this.quizType;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public int getQuestionCount() {
        return this.questionCount;
    }

    public long getTimeLimitMin() {
        return this.timeLimitMin;
    }

    public List<Question> getQuestions() {
        return this.questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setQuiz(this);  // 연관관계 주인 반영: 양방향 연관관계 유지
    }

    public int getQuestionSize() {
        return this.questions.size();
    }
}
