package com.hamplz.quizjam.quizroom.repository;

import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRoomRepository extends JpaRepository<QuizRoom, Long> {
    Optional<QuizRoom> findByInviteCode(String inviteCode);
}
