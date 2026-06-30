package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quizroom.dto.QuizEventMessage;
import com.hamplz.quizjam.quizroom.dto.RoomEventMessage;
import jakarta.annotation.PreDestroy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class QuizRoomCleanupService {

    private static final long CLEANUP_DELAY_SECONDS = 60;
    private static final long WAITING_ROOM_CLEANUP_DELAY_SECONDS = 10;

    private final QuizRoomSerivce quizRoomService;
    private final ParticipantSessionRegistry participantSessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<Long, ScheduledFuture<?>> cleanupTasks = new ConcurrentHashMap<>();

    public QuizRoomCleanupService(
        QuizRoomSerivce quizRoomService,
        ParticipantSessionRegistry participantSessionRegistry,
        SimpMessagingTemplate messagingTemplate
    ) {
        this.quizRoomService = quizRoomService;
        this.participantSessionRegistry = participantSessionRegistry;
        this.messagingTemplate = messagingTemplate;
    }

    public void scheduleCleanup(Long roomId) {
        ScheduledFuture<?> task = scheduler.schedule(
            () -> closeRoom(roomId),
            CLEANUP_DELAY_SECONDS,
            TimeUnit.SECONDS
        );
        replaceCleanupTask(roomId, task);
    }

    public void scheduleWaitingRoomCleanup(Long roomId) {
        ScheduledFuture<?> task = scheduler.schedule(
            () -> closeRoom(roomId),
            WAITING_ROOM_CLEANUP_DELAY_SECONDS,
            TimeUnit.SECONDS
        );
        replaceCleanupTask(roomId, task);
    }

    private void replaceCleanupTask(Long roomId, ScheduledFuture<?> task) {
        ScheduledFuture<?> previous = cleanupTasks.put(roomId, task);
        if (previous != null) {
            previous.cancel(false);
        }
    }

    public void cancelCleanup(Long roomId) {
        ScheduledFuture<?> task = cleanupTasks.remove(roomId);
        if (task != null) {
            task.cancel(false);
        }
    }

    private void closeRoom(Long roomId) {
        cleanupTasks.remove(roomId);
        Map<String, Long> payload = Map.of("roomId", roomId);
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId,
            RoomEventMessage.of("ROOM_CLOSED", payload)
        );
        messagingTemplate.convertAndSend(
            "/topic/quiz/" + roomId,
            QuizEventMessage.of("ROOM_CLOSED", payload)
        );
        participantSessionRegistry.removeRoom(roomId);
        quizRoomService.deleteRoomIfExists(roomId);
    }

    @PreDestroy
    public void shutdown() {
        cleanupTasks.values().forEach(task -> task.cancel(false));
        scheduler.shutdownNow();
    }
}
