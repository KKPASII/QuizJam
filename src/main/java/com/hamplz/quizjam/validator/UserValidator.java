package com.hamplz.quizjam.validator;

import com.hamplz.quizjam.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {
    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


}
