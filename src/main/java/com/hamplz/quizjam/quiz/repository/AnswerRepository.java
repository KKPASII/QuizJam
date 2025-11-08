package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
