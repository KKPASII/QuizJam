package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.value.Score;
import jakarta.persistence.*;

@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long userId; // null이면 익명 참여자

    @Column(nullable = false)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private QuizRoom room;

    @Column(nullable = false)
    private boolean host;

    @Embedded
    private Score score = Score.zero(); // 누적 점수

    protected Participant() {}

    private Participant(Long userId, String nickname,  boolean host) {
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

    public Long getHostId() {
        return this.userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void calculateScore(int score) {
        this.score.add(score);
    }
}