package com.hamplz.quizjam.quizplay.repository;

import com.hamplz.quizjam.quizplay.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByRoomIdAndParticipantId(Long roomId, Long participantId);

    List<Submission> findAllByRoomIdOrderBySubmitOrderAsc(Long roomId);

    long countByRoomId(Long roomId);
}
