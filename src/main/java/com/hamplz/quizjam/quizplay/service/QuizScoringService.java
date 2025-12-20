package com.hamplz.quizjam.quizplay.service;

import com.hamplz.quizjam.quizplay.entity.Submission;
import com.hamplz.quizjam.quizplay.repository.SubmissionRepository;
import com.hamplz.quizjam.quizroom.repository.ParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuizScoringService {
    private final SubmissionRepository submissionRepository;
    private final ParticipantRepository participantRepository;

    public QuizScoringService(SubmissionRepository submissionRepository, ParticipantRepository participantRepository) {
        this.submissionRepository = submissionRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * 한 문제 종료 후 점수 계산
     */
    public void calculateScore(Long roomId, Long questionId) {
        // 퀴즈룸 id & 질문 id로 제출된 답들을 오름차순으로 가져오기
//        List<Submission> submissions =
//                submissionRepository.findByRoomIdAndQuestionIdOrderBySubmitOrderAsc(
//                        roomId, questionId
//                );

        // TODO
        // 1. correct == true 필터
        // 2. submitOrder 기준으로 점수 차등
        // 3. participant 점수 누적
    }
}
