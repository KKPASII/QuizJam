package com.hamplz.quizjam.quizroom.entity;

import com.hamplz.quizjam.exception.ConflictException;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.ForbiddenException;
import com.hamplz.quizjam.value.Participants;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "quiz_rooms")
public class QuizRoom {

    public static final int DEFAULT_QUESTION_TIME_SECONDS = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Long hostUserId;

    @Column(unique = true, nullable = false, length = 10)
    private String inviteCode;

    @Column(nullable = false)
    private int questionTimeLimitSeconds = DEFAULT_QUESTION_TIME_SECONDS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizRoomStatus status = QuizRoomStatus.WAITING;

    @Embedded
    private Participants participants = new Participants();

    protected QuizRoom() {
    }

    private QuizRoom(Long quizId, Long hostUserId, String inviteCode, int questionTimeLimitSeconds) {
        this.quizId = quizId;
        this.hostUserId = hostUserId;
        this.inviteCode = inviteCode;
        this.questionTimeLimitSeconds = questionTimeLimitSeconds;
    }

    public static QuizRoom create(
        Long quizId,
        Long hostUserId,
        String hostNickname,
        String inviteCode,
        int questionTimeLimitSeconds
    ) {
        QuizRoom room = new QuizRoom(quizId, hostUserId, inviteCode, questionTimeLimitSeconds);
        Participant host = room.participants.createHost(hostUserId, hostNickname);
        host.assignRoom(room);
        room.participants.add(host);
        return room;
    }

    public Participant joinAnonymous(String nickname) {
        ensureCanJoin();
        Participant anonymous = participants.createAnonymous(nickname);
        anonymous.assignRoom(this);
        participants.add(anonymous);
        return anonymous;
    }

    public void updateQuiz(Long requestUserId, Long quizId, int questionTimeLimitSeconds) {
        validateHost(requestUserId);
        ensureCanJoin();
        this.quizId = quizId;
        this.questionTimeLimitSeconds = questionTimeLimitSeconds;
    }

    public void start(Long requestUserId) {
        validateHost(requestUserId);
        this.status = this.status.start();
    }

    public void finish(Long requestUserId) {
        validateHost(requestUserId);
        this.status = this.status.finish();
    }

    public void leaveParticipant(Long participantId) {
        participants.getValues().stream()
            .filter(participant -> participant.getId() != null)
            .filter(participant -> participant.getId().equals(participantId))
            .findFirst()
            .ifPresent(Participant::leave);
    }

    private void ensureCanJoin() {
        if (!this.status.canJoin()) {
            throw new ConflictException(ErrorCode.QUIZ_ROOM_ALREADY_STARTED);
        }
    }

    private void validateHost(Long requestUserId) {
        if (!this.hostUserId.equals(requestUserId)) {
            throw new ForbiddenException(ErrorCode.QUIZ_ROOM_HOST_ONLY);
        }
    }

    public int countParticipants() {
        return participants.size();
    }

    public Long getId() {
        return id;
    }

    public Long getQuizId() {
        return quizId;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public QuizRoomStatus getStatus() {
        return status;
    }

    public int getQuestionTimeLimitSeconds() {
        return questionTimeLimitSeconds;
    }

    public Participant getHostParticipant() {
        return participants.getHost();
    }

    public List<Participant> getParticipants() {
        return participants.getValues().stream()
            .sorted(Comparator.comparing(Participant::isHost).reversed())
            .toList();
    }
}
