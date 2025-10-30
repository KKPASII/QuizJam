package com.hamplz.quizjam.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class QuizRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id")
    private User hostUser;

    private String inviteCode;      // 초대 코드

    private String status;          // WAITING, IN_PROGRESS, FINISHED

    private int totalTimeSec;       // 제한 시간

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSession> sessions = new ArrayList<>();

    protected QuizRoom() {}
}
