package com.hamplz.quizjam.quizroom.repository;


import com.hamplz.quizjam.quizroom.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
