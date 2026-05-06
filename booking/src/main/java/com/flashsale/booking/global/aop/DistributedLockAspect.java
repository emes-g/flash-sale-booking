package com.flashsale.booking.global.aop;

import com.flashsale.booking.global.annotation.DistributedLock;
import com.flashsale.booking.global.exception.BusinessException;
import com.flashsale.booking.global.utils.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @DistributedLock 어노테이션이 선언된 메서드에 대해
 * Redisson 기반의 분산 락(Fair Lock)을 적용하는 AOP 클래스.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    /**
     * @DistributedLock이 붙은 메서드에 대해서만 분산 락 적용
     *
     * @param joinPoint 프록시가 가로챈 진짜 타겟 객체의 메서드 정보를 보유
     * @return  프록시가 가로챈 진짜 타겟 객체의 핵심 로직 수행
     * @throws Throwable    락 해제
     */
    @Around("@annotation(com.flashsale.booking.global.annotation.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // 1. SpEL을 이용해 동적으로 락 키 생성 (예: LOCK:ACCOMMODATION:1)
        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

        // 2. 공정성(선착순) 보장을 위한 FairLock 객체 획득 (먼저 요청한 스레드가 먼저 락을 획득)
        RLock lock = redissonClient.getFairLock(key);

        try {
            // 3. 락 획득 시도 (waitTime 동안 대기, 획득 시 leaseTime 동안 유지)
            boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());

            if (!available) {
                log.warn("락 획득 실패 (대기 시간 초과): {}", key);
                throw new BusinessException("현재 접속자가 많아 처리가 지연되고 있습니다. 다시 시도해주세요.");
            }

            // 4. 락 획득 성공 시 실제 타겟(비즈니스) 메서드 실행
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            // 락 대기 중 스레드 인터럽트 발생 시 처리
            Thread.currentThread().interrupt();
            throw new BusinessException("서버에 일시적인 오류가 발생했습니다.");
        } finally {
            // 5. 락 해제 (안전장치: 락이 잠겨있고, 현재 락을 소유한 스레드일 경우에만 해제)
            try {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (IllegalMonitorStateException e) {
                log.info("이미 해제된 락입니다. key: {}", key);
            }
        }
    }
}