package com.hamplz.quizjam.quiz.repository;

import com.hamplz.quizjam.quiz.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // ✅ 특정 퀴즈의 모든 정답 조회 (Question ID 기준 정렬)
    @Query("""
        SELECT a
        FROM Answer a
        JOIN a.question q
        WHERE q.quiz.id = :quizId
        ORDER BY q.id ASC
    """)
    List<Answer> findAllByQuizIdOrderByQuestionId(@Param("quizId") Long quizId);
}
