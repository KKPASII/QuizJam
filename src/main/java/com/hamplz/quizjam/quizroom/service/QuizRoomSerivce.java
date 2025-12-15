package com.hamplz.quizjam.quizroom.service;

import com.hamplz.quizjam.quiz.entity.Quiz;
import com.hamplz.quizjam.quiz.repository.QuizRepository;
import com.hamplz.quizjam.quizroom.dto.QuizRoomResponse;
import com.hamplz.quizjam.quizroom.entity.QuizRoom;
import com.hamplz.quizjam.quizroom.repository.QuizRoomRepository;
import com.hamplz.quizjam.user.User;
import com.hamplz.quizjam.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QuizRoomSerivce {
    private static final Logger log = LoggerFactory.getLogger(QuizRoomSerivce.class);

    private final QuizRoomRepository quizRoomRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizRoomSerivce(QuizRoomRepository quizRoomRepository, UserRepository userRepository, QuizRepository quizRepository) {
        this.quizRoomRepository = quizRoomRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    /** ✅ 호스트 방 생성 (로그인 필수) */
    public QuizRoomResponse createRoom(Long userId, Long quizId) {
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈 없음"));

        QuizRoom room = QuizRoom.create(quizId, userId, host.getNickname(), generateInviteCode());

        quizRoomRepository.save(room);

        return QuizRoomResponse.from(room);
    }

    /** ✅ 익명 참가 */
    public QuizRoomResponse join(String inviteCode, String nickname) {
        QuizRoom room = quizRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        room.joinAnonymous(nickname);

        return QuizRoomResponse.from(room);
    }

    public QuizRoomResponse getRoomByInviteCode(String inviteCode) {
        QuizRoom room = quizRoomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        return QuizRoomResponse.from(room);
    }

    public QuizRoomResponse startGame(Long roomId, Long requestUserId) {
        QuizRoom room = quizRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        room.start(requestUserId);
        return QuizRoomResponse.from(room);
    }

    public QuizRoomResponse finishGame(Long roomId, Long requestUserId) {
        QuizRoom room = quizRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방 없음"));

        room.finish(requestUserId);
        return QuizRoomResponse.from(room);
    }

    public void deleteRoom(Long roomId) {
        quizRoomRepository.deleteById(roomId);
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
