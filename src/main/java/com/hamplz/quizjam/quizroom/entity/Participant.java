package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.value.Score;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private QuizRoom room;

    @Column(nullable = false)
    private boolean host;

    @Embedded
    private Score score = Score.zero();

    protected Participant() {
    }

    private Participant(Long userId, String nickname, boolean host) {
        this.userId = userId;
        this.nickname = nickname;
        this.host = host;
    }

    public static Participant host(Long userId, String nickname) {
        return new Participant(userId, nickname, true);
    }

    public static Participant anonymous(String nickname) {
        return new Participant(null, nickname, false);
    }

    public void assignRoom(QuizRoom room) {
        this.room = room;
    }

    public boolean isHost() {
        return this.host;
    }

    public boolean isAnonymous() {
        return userId == null;
    }

    public void calculateScore(int score) {
        this.score.add(score);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public QuizRoom getRoom() {
        return room;
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score.getTotalScore();
    }
}
