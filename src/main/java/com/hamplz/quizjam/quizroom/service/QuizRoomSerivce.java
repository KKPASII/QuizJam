package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.ForbiddenException;
import com.hamplz.quizjam.exception.NotFoundException;
import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.quizroom.dto.CreateRoomRequest;
import com.hamplz.quizjam.quizroom.dto.JoinRoomResponse;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.entity.Participant;
import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.repository.QuizRoomRepository;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class QuizRoomSerivce {

    private static final int INVITE_CODE_LENGTH = 8;
    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();
    private final QuizRoomRepository quizRoomRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizRoomSerivce(
        QuizRoomRepository quizRoomRepository,
        UserRepository userRepository,
        QuizRepository quizRepository
    ) {
        this.quizRoomRepository = quizRoomRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public QuizRoomResponse createRoom(Long userId, CreateRoomRequest request) {
        User host = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        ensureQuizOwner(userId, request.quizId());

        QuizRoom room = QuizRoom.create(
            request.quizId(),
            userId,
            host.getNickname(),
            generateInviteCode(),
            normalizeTimeLimit(request.questionTimeLimitSeconds())
        );

        QuizRoom savedRoom = quizRoomRepository.saveAndFlush(room);
        return toResponse(savedRoom);
    }

    @Transactional(readOnly = true)
    public QuizRoomResponse getRoom(Long roomId) {
        return toResponse(findRoom(roomId));
    }

    @Transactional(readOnly = true)
    public QuizRoomResponse getRoomByInviteCode(String inviteCode) {
        return toResponse(findRoomByInviteCode(inviteCode));
    }

    @Transactional
    public JoinRoomResponse join(String inviteCode, String nickname) {
        QuizRoom room = findRoomByInviteCode(inviteCode);
        String resolvedNickname = resolveNickname(room, nickname);
        Participant participant = room.joinAnonymous(resolvedNickname);

        QuizRoom savedRoom = quizRoomRepository.saveAndFlush(room);
        return new JoinRoomResponse(
            toResponse(savedRoom),
            participant.getId(),
            participant.getNickname()
        );
    }

    @Transactional
    public QuizRoomResponse updateRoomQuiz(Long roomId, Long userId, CreateRoomRequest request) {
        ensureQuizOwner(userId, request.quizId());
        QuizRoom room = findRoom(roomId);
        room.updateQuiz(userId, request.quizId(), normalizeTimeLimit(request.questionTimeLimitSeconds()));
        return toResponse(room);
    }

    @Transactional
    public QuizRoomResponse startGame(Long roomId, Long requestUserId) {
        QuizRoom room = findRoom(roomId);
        room.start(requestUserId);
        return toResponse(room);
    }

    @Transactional
    public QuizRoomResponse finishGame(Long roomId, Long requestUserId) {
        QuizRoom room = findRoom(roomId);
        room.finish(requestUserId);
        return toResponse(room);
    }

    @Transactional
    public QuizRoomResponse leave(Long roomId, Long participantId) {
        QuizRoom room = findRoom(roomId);
        room.leaveParticipant(participantId);
        return toResponse(room);
    }

    @Transactional
    public LeaveRoomResult leaveAndCloseWaitingRoomIfNeeded(Long roomId, Long participantId) {
        QuizRoom room = findRoom(roomId);
        room.leaveParticipant(participantId);
        if (room.shouldCloseWaitingRoom()) {
            quizRoomRepository.delete(room);
            return LeaveRoomResult.closed(roomId);
        }
        return LeaveRoomResult.open(toResponse(room));
    }

    @Transactional
    public void deleteRoom(Long roomId, Long requestUserId) {
        QuizRoom room = findRoom(roomId);
        if (!room.getHostUserId().equals(requestUserId)) {
            throw new ForbiddenException(ErrorCode.QUIZ_ROOM_HOST_ONLY);
        }
        quizRoomRepository.delete(room);
    }

    @Transactional
    public void deleteRoomIfExists(Long roomId) {
        quizRoomRepository.findById(roomId)
            .ifPresent(quizRoomRepository::delete);
    }

    private QuizRoom findRoom(Long roomId) {
        return quizRoomRepository.findById(roomId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.QUIZ_ROOM_NOT_FOUND));
    }

    private QuizRoom findRoomByInviteCode(String inviteCode) {
        return quizRoomRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new NotFoundException(ErrorCode.QUIZ_ROOM_NOT_FOUND));
    }

    private QuizRoomResponse toResponse(QuizRoom room) {
        Quiz quiz = quizRepository.findById(room.getQuizId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.QUIZ_NOT_FOUND));
        return QuizRoomResponse.from(room, quiz);
    }

    private void ensureQuizOwner(Long userId, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.QUIZ_NOT_FOUND));
        if (!quiz.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.QUIZ_ROOM_HOST_ONLY);
        }
    }

    private int normalizeTimeLimit(Integer questionTimeLimitSeconds) {
        return questionTimeLimitSeconds == null
            ? QuizRoom.DEFAULT_QUESTION_TIME_SECONDS
            : questionTimeLimitSeconds;
    }

    private String resolveNickname(QuizRoom room, String nickname) {
        String normalized = normalizeNickname(nickname);
        if (normalized != null && isNicknameAvailable(room, normalized)) {
            return normalized;
        }

        for (int i = 0; i < 100; i++) {
            String candidate = "Guest-" + (1000 + random.nextInt(9000));
            if (isNicknameAvailable(room, candidate)) {
                return candidate;
            }
        }
        return "Guest-" + System.currentTimeMillis();
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return null;
        }
        String trimmed = nickname.trim();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) : trimmed;
    }

    private boolean isNicknameAvailable(QuizRoom room, String nickname) {
        String lowerNickname = nickname.toLowerCase(Locale.ROOT);
        return room.getParticipants().stream()
            .noneMatch(participant -> participant.getNickname().toLowerCase(Locale.ROOT).equals(lowerNickname));
    }

    private String generateInviteCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                builder.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (quizRoomRepository.existsByInviteCode(code));
        return code;
    }
}
