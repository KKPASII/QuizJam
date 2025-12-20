package com.hamplz.quizjam.quizplay.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}")
public class QuizRoundController {

    @PutMapping("/")
}
