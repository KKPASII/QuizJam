package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findAllByUserId(Long userId, Pageable pageable);
}