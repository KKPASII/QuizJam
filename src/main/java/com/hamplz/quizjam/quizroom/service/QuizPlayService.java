package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.service.QuizQueryService;
import com.hamplz.quizjam.quizroom.dto.AnswerSubmittedMessage;
import com.hamplz.quizjam.quizroom.dto.ParticipantRankingResponse;
import com.hamplz.quizjam.quizroom.dto.QuestionClosedMessage;
import com.hamplz.quizjam.quizroom.dto.QuestionOpenedMessage;
import com.hamplz.quizjam.quizroom.dto.QuestionsFinishedMessage;
import com.hamplz.quizjam.quizroom.dto.QuizEventMessage;
import com.hamplz.quizjam.quizroom.dto.QuizResultMessage;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class QuizPlayService {

    private final QuizQueryService quizQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<Long, GameState> games = new ConcurrentHashMap<>();
    private final Map<Long, ConcurrentMap<Long, Integer>> results = new ConcurrentHashMap<>();

    public QuizPlayService(
        QuizQueryService quizQueryService,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.quizQueryService = quizQueryService;
        this.messagingTemplate = messagingTemplate;
    }

    public void startGame(QuizRoomResponse room) {
        List<Question> questions = quizQueryService.getQuizQuestions(room.quizId());
        if (questions.isEmpty()) {
            throw new IllegalStateException("Quiz has no questions.");
        }

        GameState previous = games.put(room.roomId(), new GameState(room, questions));
        if (previous != null) {
            previous.cancelTimer();
        }
        results.remove(room.roomId());

        broadcast(room.roomId(), QuizEventMessage.of("QUIZ_STARTED", Map.of(
            "roomId", room.roomId(),
            "quizId", room.quizId(),
            "questionCount", questions.size(),
            "questionTimeLimitSeconds", room.questionTimeLimitSeconds()
        )));
        openQuestion(room.roomId());
    }

    public void submitAnswer(Long roomId, Long participantId, Long questionId, String answer) {
        GameState state = games.get(roomId);
        if (state == null) {
            throw new IllegalStateException("Quiz is not in progress.");
        }

        Question currentQuestion = state.currentQuestion();
        if (currentQuestion == null) {
            throw new IllegalStateException("No open question.");
        }
        if (!Objects.equals(currentQuestion.getId(), questionId)) {
            throw new IllegalStateException("Submitted question is not current question.");
        }
        if (Instant.now().toEpochMilli() > state.deadlineEpochMs) {
            throw new IllegalStateException("Question deadline has passed.");
        }

        SubmissionSnapshot submission = new SubmissionSnapshot(participantId, normalizeAnswer(answer), Instant.now().toEpochMilli());
        SubmissionSnapshot previous = state.submissions.putIfAbsent(participantId, submission);
        if (previous != null) {
            throw new IllegalStateException("Participant already submitted this question.");
        }

        broadcast(roomId, QuizEventMessage.of("ANSWER_SUBMITTED", new AnswerSubmittedMessage(
            roomId,
            state.questionIndex,
            questionId,
            participantId,
            state.submissions.size()
        )));
    }

    public void submitResult(QuizRoomResponse room, Long participantId, int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score must be zero or positive.");
        }
        if (!isRoomParticipant(room, participantId)) {
            throw new IllegalStateException("Participant is not in requested room.");
        }

        ConcurrentMap<Long, Integer> roomResults = results.computeIfAbsent(room.roomId(), ignored -> new ConcurrentHashMap<>());
        Integer previous = roomResults.putIfAbsent(participantId, score);
        if (previous != null) {
            throw new IllegalStateException("Participant already submitted result.");
        }

        int expectedCount = room.participants().size();
        int submittedCount = roomResults.size();
        boolean finalized = submittedCount >= expectedCount;
        QuizResultMessage message = new QuizResultMessage(
            room.roomId(),
            submittedCount,
            expectedCount,
            finalized,
            buildRankings(room, roomResults)
        );

        broadcast(room.roomId(), QuizEventMessage.of(finalized ? "RESULT_FINALIZED" : "RESULT_UPDATED", message));
    }

    public void forceFinish(Long roomId) {
        GameState state = games.remove(roomId);
        if (state != null) {
            state.cancelTimer();
            broadcastQuestionsFinished(state);
        }
    }

    private void openQuestion(Long roomId) {
        GameState state = games.get(roomId);
        if (state == null) {
            return;
        }

        if (state.questionIndex >= state.questions.size()) {
            finishAllQuestions(state);
            return;
        }

        state.submissions.clear();
        Question question = state.questions.get(state.questionIndex);
        long deadlineEpochMs = Instant.now()
            .plusSeconds(state.room.questionTimeLimitSeconds())
            .toEpochMilli();
        state.deadlineEpochMs = deadlineEpochMs;

        broadcast(roomId, QuizEventMessage.of("QUESTION_OPENED", new QuestionOpenedMessage(
            roomId,
            state.room.quizId(),
            state.questionIndex,
            state.questions.size(),
            question.getId(),
            question.getQuestionText(),
            question.getOptions(),
            question.getHint(),
            deadlineEpochMs
        )));

        state.cancelTimer();
        state.timer = scheduler.schedule(
            () -> closeQuestion(roomId, state.questionIndex),
            state.room.questionTimeLimitSeconds(),
            TimeUnit.SECONDS
        );
    }

    private void closeQuestion(Long roomId, int expectedQuestionIndex) {
        GameState state = games.get(roomId);
        if (state == null || state.questionIndex != expectedQuestionIndex) {
            return;
        }

        Question question = state.questions.get(state.questionIndex);
        broadcast(roomId, QuizEventMessage.of("QUESTION_CLOSED", new QuestionClosedMessage(
            roomId,
            state.room.quizId(),
            state.questionIndex,
            state.questions.size(),
            question.getId()
        )));

        state.questionIndex++;
        openQuestion(roomId);
    }

    private void finishAllQuestions(GameState state) {
        games.remove(state.room.roomId());
        state.cancelTimer();
        broadcastQuestionsFinished(state);
    }

    private void broadcastQuestionsFinished(GameState state) {
        broadcast(state.room.roomId(), QuizEventMessage.of("QUESTIONS_FINISHED", new QuestionsFinishedMessage(
            state.room.roomId(),
            state.room.quizId(),
            state.questions.size()
        )));
    }

    private void broadcast(Long roomId, QuizEventMessage message) {
        messagingTemplate.convertAndSend("/topic/quiz/" + roomId, message);
    }

    private boolean isRoomParticipant(QuizRoomResponse room, Long participantId) {
        return room.participants().stream()
            .anyMatch(participant -> Objects.equals(participant.participantId(), participantId));
    }

    private List<ParticipantRankingResponse> buildRankings(
        QuizRoomResponse room,
        ConcurrentMap<Long, Integer> roomResults
    ) {
        List<ParticipantScore> scores = room.participants().stream()
            .map(participant -> new ParticipantScore(
                participant.participantId(),
                participant.nickname(),
                roomResults.getOrDefault(participant.participantId(), 0)
            ))
            .sorted(Comparator
                .comparingInt(ParticipantScore::score).reversed()
                .thenComparing(ParticipantScore::participantId, Comparator.nullsLast(Long::compareTo)))
            .toList();

        return IntStream.range(0, scores.size())
            .mapToObj(index -> {
                ParticipantScore score = scores.get(index);
                return new ParticipantRankingResponse(
                    index + 1,
                    score.participantId(),
                    score.nickname(),
                    score.score()
                );
            })
            .toList();
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim();
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static class GameState {
        private final QuizRoomResponse room;
        private final List<Question> questions;
        private final ConcurrentMap<Long, SubmissionSnapshot> submissions = new ConcurrentHashMap<>();
        private int questionIndex;
        private long deadlineEpochMs;
        private ScheduledFuture<?> timer;

        private GameState(QuizRoomResponse room, List<Question> questions) {
            this.room = room;
            this.questions = questions;
        }

        private Question currentQuestion() {
            if (questionIndex < 0 || questionIndex >= questions.size()) {
                return null;
            }
            return questions.get(questionIndex);
        }

        private void cancelTimer() {
            if (timer != null) {
                timer.cancel(false);
                timer = null;
            }
        }
    }

    private record SubmissionSnapshot(
        Long participantId,
        String answer,
        long submittedAtEpochMs
    ) {
    }

    private record ParticipantScore(
        Long participantId,
        String nickname,
        int score
    ) {
    }
}
