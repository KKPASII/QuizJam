package com.hamplz.quizjam.quizroom.repository;

import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRoomRepository extends JpaRepository<QuizRoom, Long> {
    boolean existsByInviteCode(String inviteCode);

    @Override
    @EntityGraph(attributePaths = "participants.values")
    Optional<QuizRoom> findById(Long id);

    @EntityGraph(attributePaths = "participants.values")
    Optional<QuizRoom> findByInviteCode(String inviteCode);
}
