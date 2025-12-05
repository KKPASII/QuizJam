package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.value.Participants;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "quiz_rooms")
@AssociationOverrides({
    @AssociationOverride(name = "participants.values", joinColumns = @JoinColumn(name = "room_id"))
})
public class QuizRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_user_id", nullable = false)
    private User hostUser;

    @Column(unique = true, nullable = false, length = 10)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    private QuizRoomStatus status = QuizRoomStatus.WAITING;

    @Embedded
    private Participants participants = new Participants();

    protected QuizRoom() {}

    public static QuizRoom create(Quiz quiz, User hostUser, String inviteCode) {
        QuizRoom room = new QuizRoom();
        room.quiz = quiz;
        room.hostUser = hostUser;
        room.inviteCode = inviteCode;
        return room;
    }

    public Participant join(String nickname, User user) {
        Participant p = participants.isEmpty()
            ? Participant.joinAsHost(user)
            : (user == null ? Participant.joinAsAnonymous(nickname) : Participant.joinAsAnonymous(user.getNickname()));

        p.assignTo(this);
        participants.add(p);

        return p;
    }

    public int countParticipants() {
        return participants.size();
    }

    public Participant getHostParticipant() {
        return participants.getHost();
    }

    public List<Participant> getParticipantList() {
        return participants.getValues();
    }

    public void start() { this.status = QuizRoomStatus.IN_PROGRESS; }

    public void finish() { this.status = QuizRoomStatus.FINISHED; }

    public Long getId() { return id; }
    public Long getQuizId() { return quiz.getId(); }
    public Long getHostUserId() { return hostUser.getId(); }
    public String getInviteCode() { return inviteCode; }
    public QuizRoomStatus getStatus() { return status; }
}