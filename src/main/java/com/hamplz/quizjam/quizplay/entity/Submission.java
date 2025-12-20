package com.hamplz.quizjam.quizplay.entity;

import com.hamplz.quizjam.quizroom.entity.Participant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "submissions",
        indexes = {
                @Index(name = "idx_room_question", columnList = "roomId, questionId"),
                @Index(name = "idx_participant", columnList = "participantId")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"roomId", "questionId", "participantId"}
                )
        }
)
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "submitted_answer", nullable = false, length = 255)
    private String submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * 제출 순서(1,2,3..) - "방 안에서" 기준.
     * 이 값은 서비스에서 결정해서 넣어주는 걸 추천 (동시성 때문).
     */
    @Column(name = "submit_order", nullable = false)
    private int submitOrder;

    /**
     * 이 제출로 얻은 점수 (누적점수는 Participant.score에 반영)
     */
    @Column(name = "awarded_score", nullable = false)
    private int awardedScore;

    protected Submission() {}

    private Submission(Long roomId, Long participantId, String submittedAnswer, boolean correct, LocalDateTime submittedAt) {
        this.roomId = roomId;
        this.participantId = participantId;
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.submittedAt = submittedAt;
    }

    private Submission(
            Long roomId,
            Long participantId,
            Long questionId,
            String submittedAnswer,
            boolean correct,
            LocalDateTime submittedAt,
            int submitOrder
    ) {
        this.roomId = roomId;
        this.participantId = participantId;
        this.questionId = questionId;
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.submittedAt = submittedAt;
        this.submitOrder = submitOrder;
        this.awardedScore = 0;
    }

    public static Submission create(
            Long roomId,
            Long participantId,
            Long questionId,
            String submittedAnswer,
            boolean correct,
            LocalDateTime submittedAt,
            int submitOrder
    ) {
        return new Submission(roomId, participantId, questionId, submittedAnswer, correct, submittedAt, submitOrder);
    }

    public boolean isCorrect() {
        return this.correct;
    }

    public LocalDateTime getSubmittedAt() {
        return this.submittedAt;
    }
}
