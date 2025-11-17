package com.hamplz.quizjam.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(com.hamplz.quizjam.common.aop.ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();

        Object result = joinPoint.proceed(); // 실제 메서드 실행

        long durationNs = System.nanoTime() - start;
        double durationMs = durationNs / 1_000_000.0;
        double durationSec = durationNs / 1_000_000_000.0;

        log.info("⏱ [{}] 실행 시간: {} ns (≈ {} ms, {} s)",
            joinPoint.getSignature().toShortString(),
            durationNs, String.format("%.3f", durationMs), String.format("%.3f", durationSec));

        return result;
    }
}
