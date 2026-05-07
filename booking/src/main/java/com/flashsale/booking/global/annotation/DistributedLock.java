package com.flashsale.booking.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // SpEL 문법을 이용해 런타임에 동적으로 락의 고유 키를 생성하기 위한 속성
    // 예: key = "'ACCOMMODATION:' + #accommodationId" -> "ACCOMMODATION:1" 로 치환됨
    String key();

    // 락 획득을 위해 기다리는 최대 시간 (이 시간 내에 못 얻으면 예외 발생)
    long waitTime() default 5L;

    // 락을 획득한 후의 최대 유지 시간 (서버 다운 시 데드락을 방지하기 위한 안전장치)
    // 실제로는 로직이 끝나는 즉시(수 밀리초 내에) finally에서 락이 해제됨.
    long leaseTime() default 3L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}