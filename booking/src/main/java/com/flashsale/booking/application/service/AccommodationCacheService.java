package com.flashsale.booking.application.service;

import com.flashsale.booking.domain.accommodation.dto.AccommodationCacheDto;
import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import com.flashsale.booking.domain.accommodation.repository.AccommodationRepository;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationCacheService {

    private final AccommodationRepository accommodationRepository;

    // Redis 캐싱 전략 적용 메서드
    // cacheNames = "accommodation": Redis에 저장될 캐시의 논리적 그룹 이름
    // key = "#accommodationId": 요청된 숙소 ID를 Redis의 Key값으로 사용 (예: accommodation::1)
    //
    // [동작 원리]
    // 1. AOP 프록시가 먼저 Redis를 확인하여 데이터가 있으면 즉시 반환(Cache Hit)하고, 메서드 내부 로직은 무시
    // 2. Redis에 데이터가 없으면(Cache Miss), 메서드 내부의 DB 조회 로직을 실행한 뒤 반환값을 Redis에 저장(캐싱)하고 응답
    @Cacheable(cacheNames = "accommodation", key = "#accommodationId")
    @Transactional(readOnly = true)
    public AccommodationCacheDto getAccommodationInfo(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 숙소입니다."));

        // Entity 대신 DTO로 변환하여 캐싱 (직렬화에 안전한 POJO기 때문)
        return AccommodationCacheDto.from(accommodation);
    }
}