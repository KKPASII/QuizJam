package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quiz.entity.Question;
import com.hamplz.quizjam.quiz.service.QuizQueryService;
import com.hamplz.quizjam.quizroom.dto.QuestionClosedMessage;
import com.hamplz.quizjam.quizroom.dto.QuestionOpenedMessage;
import com.hamplz.quizjam.quizroom.dto.QuestionsFinishedMessage;
import com.hamplz.quizjam.quizroom.dto.QuizEventMessage;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class QuizPlayService {

    private final QuizQueryService quizQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<Long, GameState> games = new ConcurrentHashMap<>();

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

        broadcast(room.roomId(), QuizEventMessage.of("QUIZ_STARTED", Map.of(
            "roomId", room.roomId(),
            "quizId", room.quizId(),
            "questionCount", questions.size(),
            "questionTimeLimitSeconds", room.questionTimeLimitSeconds()
        )));
        openQuestion(room.roomId());
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

        Question question = state.questions.get(state.questionIndex);
        long deadlineEpochMs = Instant.now()
            .plusSeconds(state.room.questionTimeLimitSeconds())
            .toEpochMilli();

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

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    private static class GameState {
        private final QuizRoomResponse room;
        private final List<Question> questions;
        private int questionIndex;
        private ScheduledFuture<?> timer;

        private GameState(QuizRoomResponse room, List<Question> questions) {
            this.room = room;
            this.questions = questions;
        }

        private void cancelTimer() {
            if (timer != null) {
                timer.cancel(false);
                timer = null;
            }
        }
    }
}
