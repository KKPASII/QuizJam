package com.hamplz.quizjam.quizroom.repository;

import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRoomRepository extends JpaRepository<QuizRoom, Long> {
}
