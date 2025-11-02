package com.hamplz.quizjam.config;

import com.hamplz.quizjam.auth.controller.LoginUser;
import com.hamplz.quizjam.exception.ErrorCode;
import com.hamplz.quizjam.exception.UnAuthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 컨트롤러에서 @LoginUser Long userId 로 현재 로그인 사용자 ID를 주입해주는 리졸버
 */
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
            && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnAuthorizedException(ErrorCode.INVALID_USER_INFO);
        }

        Long userId = (Long) authentication.getPrincipal();

        return userId;
    }
}
