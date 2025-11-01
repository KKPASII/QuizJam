//package com.hamplz.quizjam.quiz.entity;
//
//import com.hamplz.quizjam.user.User;
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "quiz_rooms")
//public class QuizRoom {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    private Quiz quiz;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "host_user_id", nullable = false)
//    private User hostUser;
//
//    @Column(unique = true, nullable = false, length = 10)
//    private String inviteCode;      // 초대 코드
//
//    @Enumerated(EnumType.STRING)
//    private QuizRoomStatus status = QuizRoomStatus.WAITING;
//
//    private Long totalTimeSec;       // 제한 시간
//
//    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<QuizSession> sessions = new ArrayList<>();
//
//    protected QuizRoom() {}
//
//    private QuizRoom(Quiz quiz, User hostUser) {}
//
//    public static QuizRoom create(Quiz quiz, User hostUser, String inviteCode, int totalTimeSec) {
//        QuizRoom room = new QuizRoom();
//        room.quiz = quiz;
//        room.hostUser = hostUser;
//        room.inviteCode = inviteCode;
//        room.totalTimeSec = totalTimeSec;
//        room.status = QuizRoomStatus.WAITING;
//        return room;
//    }
//
//    public void addSession(QuizSession session) {
//        sessions.add(session);
//        session.setRoom(this);
//    }
//
//    public void removeSession(QuizSession session) {
//        sessions.remove(session);
//        session.setRoom(null);
//    }
//
//    public void start() {
//        this.status = QuizRoomStatus.IN_PROGRESS;
//    }
//
//    public void finish() {
//        this.status = QuizRoomStatus.FINISHED;
//    }
//}
