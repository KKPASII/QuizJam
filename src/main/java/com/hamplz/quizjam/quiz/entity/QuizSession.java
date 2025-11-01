//package com.hamplz.quizjam.quiz.entity;
//
//import com.hamplz.quizjam.guest.Guest;
//import com.hamplz.quizjam.user.User;
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//public class QuizSession {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "guest_id")
//    private Guest guest;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "room_id")
//    private QuizRoom room;
//
//    private int score;
//    private int rank;
//
//    private LocalDateTime joinedAt = LocalDateTime.now();
//    private LocalDateTime finishedAt;
//
//}
