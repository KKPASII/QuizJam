package com.hamplz.quizjam.user;

import com.hamplz.quizjam.user.dto.UserCreateRequest;
import com.hamplz.quizjam.user.dto.UserInfoResponse;
import com.hamplz.quizjam.user.dto.UserUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserInfoResponse create(UserCreateRequest userCreateRequest) {
        User user = User.create(
                userCreateRequest.nickname(),
                Long.valueOf(userCreateRequest.kakaoId())
        );

        userRepository.save(user);
        return new UserInfoResponse(user.getNickname());
    }

    public UserInfoResponse get(Long id) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        return new UserInfoResponse(user.getNickname());
    }

    public UserInfoResponse update(Long id, UserUpdateRequest userUpdateRequest) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        user.update(userUpdateRequest.nickname());

        return new UserInfoResponse(userUpdateRequest.nickname());
    }

    public void delete(Long id) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        userRepository.delete(user);
    }

    public User findUserByKakaoId(Long kakaoId, String nickname) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.create(nickname, kakaoId)));
    }
}
