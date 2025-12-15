package com.hamplz.quizjam.quizroom.entity;

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

    @Column(nullable = false)
    private Long quizId; // 🔥 Quiz 객체 대신 ID만

    @Column(nullable = false)
    private Long hostUserId;

    @Column(unique = true, nullable = false, length = 10)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    private QuizRoomStatus status = QuizRoomStatus.WAITING;

    @Embedded
    private Participants participants = new Participants();

    protected QuizRoom() {}

    private QuizRoom(Long quizId, Long hostUserId, String inviteCode) {
        this.quizId = quizId;
        this.hostUserId = hostUserId;
        this.inviteCode = inviteCode;
    }

    public static QuizRoom create(Long quizId, Long hostUserId, String hostNickname, String inviteCode) {
        QuizRoom room = new QuizRoom(quizId, hostUserId, inviteCode);

        Participant host = room.participants.createHost(hostUserId, hostNickname);
        host.assignRoom(room);
        room.participants.add(host);

        return room;
    }

    public void joinAnonymous(String nickname) {
        Participant anonymous = Participant.anonymous(nickname);
        anonymous.assignRoom(this);
        participants.add(anonymous);
    }

    public void start() { this.status = QuizRoomStatus.IN_PROGRESS; }

    public void finish() { this.status = QuizRoomStatus.FINISHED; }

    public int countParticipants() {
        return participants.size();
    }

    public Long getId() { return id; }
    public Long getQuizId() { return quizId; }
    public Long getHostUserId() { return hostUserId; }
    public String getInviteCode() { return inviteCode; }
    public QuizRoomStatus getStatus() { return status; }

    public Participant getHostParticipant() {
        return participants.getHost();
    }

    public List<Participant> getParticipants() {
        return participants.getValues();
    }
}