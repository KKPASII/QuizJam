package com.hamplz.quizjam.user;

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

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoResponse> get(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(userService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserInfoResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        return ResponseEntity.ok(userService.update(id, userUpdateRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<UserInfoResponse> delete(
            @PathVariable("id") Long id
    ) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
