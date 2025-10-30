package com.hamplz.quizjam.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    @OneToMany(mappedBy = "user")
    private List<QuizSession> sessions = new ArrayList<>();

    protected User() {}
}
