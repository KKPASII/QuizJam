package com.hamplz.quizjam.quizroom.repository;

import com.hamplz.quizjam.quizroom.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByRoomIdAndId(Long roomId, Long id);
}
