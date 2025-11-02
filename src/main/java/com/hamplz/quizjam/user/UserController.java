package com.hamplz.quizjam.user;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.user.dto.UserCreateRequest;
import com.hamplz.quizjam.user.dto.UserInfoResponse;
import com.hamplz.quizjam.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserInfoResponse> create(
            @Valid @RequestBody UserCreateRequest userCreateRequest
    ) {
        return ResponseEntity.ok(userService.create(userCreateRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> get(
        @LoginUser Long userId
    ) {
        return ResponseEntity.ok(userService.get(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> update(
        @LoginUser Long userId,
        @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        return ResponseEntity.ok(userService.update(userId, userUpdateRequest));
    }

    @DeleteMapping("/me")
    public ResponseEntity<UserInfoResponse> delete(
        @LoginUser Long userId
    ) {
        userService.delete(userId);

        return ResponseEntity.noContent().build();
    }
}
