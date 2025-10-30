package com.hamplz.quizjam.repository;

import com.hamplz.quizjam.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
