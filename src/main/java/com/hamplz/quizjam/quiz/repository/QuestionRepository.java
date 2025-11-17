package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByQuizId(Long quizId);
}
