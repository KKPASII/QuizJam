package com.hamplz.quizjam.entity;

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
}
