package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.user.User;
import jakarta.persistence.*;

@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // null이면 익명 참여자

    @Column(nullable = false)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private QuizRoom room;

    private boolean host;

    protected Participant() {}

    private Participant(String nickname, User user, boolean host) {
        this.nickname = nickname;
        this.user = user;
        this.host = host;
    }

    public static Participant joinAsHost(User user) {
        return new Participant(user.getNickname(), user, true);
    }

    public static Participant joinAsAnonymous(String nickname) {
        return new Participant(nickname, null, false);
    }

    public void assignTo(QuizRoom room) {
        this.room = room;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isHost() {
        return this.host;
    }
}