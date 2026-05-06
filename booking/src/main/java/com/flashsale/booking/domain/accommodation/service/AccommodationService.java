package com.flashsale.booking.domain.accommodation.service;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import com.flashsale.booking.domain.accommodation.repository.AccommodationRepository;
import com.flashsale.booking.domain.accommodation.dto.AccommodationResponse;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    // 전체 숙소 목록 조회
    public List<AccommodationResponse> getAccommodations() {
        return accommodationRepository.findAll().stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 숙소 단건 조회
    public AccommodationResponse getAccommodation(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("존재하지 않는 숙소입니다."));

        return AccommodationResponse.from(accommodation);
    }
}