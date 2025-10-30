package com.hamplz.quizjam.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class QuizSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private QuizRoom room;

    private int score;
    private int rank;

    private LocalDateTime joinedAt = LocalDateTime.now();
    private LocalDateTime finishedAt;

}
