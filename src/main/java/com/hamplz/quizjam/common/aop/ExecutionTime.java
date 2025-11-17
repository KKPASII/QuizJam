package com.hamplz.quizjam.common.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)         // 메서드에만 적용
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@Documented
public @interface ExecutionTime {
}