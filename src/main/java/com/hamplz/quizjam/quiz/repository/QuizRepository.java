package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
