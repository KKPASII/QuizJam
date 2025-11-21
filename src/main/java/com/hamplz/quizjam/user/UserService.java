package com.hamplz.quizjam.user;

import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.NotFoundException;
import com.hamplz.quizjam.user.dto.UserCreateRequest;
import com.hamplz.quizjam.user.dto.UserInfoResponse;
import com.hamplz.quizjam.user.dto.UserUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserInfoResponse create(UserCreateRequest userCreateRequest) {
        User user = User.create(
                userCreateRequest.nickname(),
                Long.valueOf(userCreateRequest.kakaoId())
        );

        userRepository.save(user);
        return new UserInfoResponse(user.getNickname());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse get(Long id) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        return new UserInfoResponse(user.getNickname());
    }

    @Transactional
    public UserInfoResponse update(Long id, UserUpdateRequest userUpdateRequest) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        user.update(userUpdateRequest.nickname());

        return new UserInfoResponse(userUpdateRequest.nickname());
    }

    @Transactional
    public void delete(Long id) {
        User user  = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional
    public User findUserByKakaoId(Long kakaoId, String nickname) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.create(nickname, kakaoId)));
    }
}
