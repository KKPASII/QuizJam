package com.hamplz.quizjam.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(unique = true)
    private Long kakaoId;

//    @OneToMany(mappedBy = "user")
//    private List<QuizSession> sessions = new ArrayList<>();

    protected User() {}

    public User(String nickname, Long kakaoId) {
        this.nickname = nickname;
        this.kakaoId = kakaoId;
    }

    public static User create(String nickname, Long kakaoId) {
        return new User(nickname, kakaoId);
    }

    public void update(String nickname) {
        this.nickname = nickname;
    }

    public Long getId() {
        return this.id;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Long getKakaoId() {
        return this.kakaoId;
    }
}
