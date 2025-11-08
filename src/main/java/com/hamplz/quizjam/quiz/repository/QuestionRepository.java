package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
