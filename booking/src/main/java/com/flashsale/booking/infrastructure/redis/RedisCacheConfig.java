package com.flashsale.booking.infrastructure.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching  // 캐싱 관련 어노테이션 활성화
@Configuration
public class RedisCacheConfig {

    // CacheManager: 캐시 저장 방식과 만료 시간 등을 설정하는 빈
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // 캐시 기본 설정 구성
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 1. 키는 String 형태로 직렬화
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 2. 값은 JSON 형태로 직렬화 (엔티티나 DTO를 JSON으로 저장)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // 3. 캐시 데이터의 만료 시간(TTL)을 10분으로 설정
                .entryTtl(Duration.ofMinutes(10));

        // 위에서 만든 설정을 바탕으로 CacheManager 객체를 생성하여 스프링에 등록
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}