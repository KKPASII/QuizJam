package com.hamplz.quizjam.auth.dto;

import com.hamplz.quizjam.auth.entity.DeviceType;

public record ClientInfo(String userAgent, DeviceType deviceType) {
}
