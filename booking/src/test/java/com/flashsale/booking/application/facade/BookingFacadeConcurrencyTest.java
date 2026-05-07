package com.flashsale.booking.application.facade;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import com.flashsale.booking.domain.accommodation.entity.AccommodationStock;
import com.flashsale.booking.domain.accommodation.repository.AccommodationRepository;
import com.flashsale.booking.domain.accommodation.repository.AccommodationStockRepository;
import com.flashsale.booking.domain.booking.dto.BookingPaymentRequest;
import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import com.flashsale.booking.domain.user.entity.User;
import com.flashsale.booking.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookingFacadeConcurrencyTest {

    @Autowired
    private BookingFacade bookingFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AccommodationStockRepository stockRepository;

    private Accommodation savedAccommodation;
    private List<User> testUsers = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // 1. 1000명의 테스트 유저 생성 (리플렉션 사용)
        for (int i = 0; i < 1000; i++) {
            User user = createInstance(User.class);
            ReflectionTestUtils.setField(user, "name", "User" + i);
            testUsers.add(userRepository.save(user));
        }

        // 2. 재고가 100개인 숙소 생성 (리플렉션 사용)
        Accommodation accommodation = createInstance(Accommodation.class);
        ReflectionTestUtils.setField(accommodation, "name", "특가 신라호텔");
        ReflectionTestUtils.setField(accommodation, "price", 50000);
        ReflectionTestUtils.setField(accommodation, "openAt", LocalDateTime.now().minusDays(1));
        savedAccommodation = accommodationRepository.save(accommodation);

        AccommodationStock stock = createInstance(AccommodationStock.class);
        ReflectionTestUtils.setField(stock, "accommodation", savedAccommodation);
        ReflectionTestUtils.setField(stock, "stock", 100);
        stockRepository.save(stock);
    }

    @Test
    @DisplayName("선착순 100명 공정성 테스트: 1000명이 동시에 결제를 요청하면 정확히 100명만 성공하고 900명은 실패해야 한다.")
    void testFirstComeFirstServedFairness() throws InterruptedException {
        int threadCount = 1000;

        // 32개의 스레드를 고정적으로 유지하는 스레드 풀 생성 (동시 요청 환경 구성)
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 1000개의 작업이 모두 완료될 때까지 메인 스레드를 대기시키기 위한 래치 생성
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티 스레드 환경에서의 정합성을 보장하는 변수인 AtomicInteger 사용
        //
        // 지역 변수인 successCount와 failCount가 람다식 내부에서 사용될 때,
        // 자바 컴파일러는 해당 변수의 참조를 힙(Heap) 영역의 람다 객체 내부로 복사(Capture)한다.
        // 결과적으로 모든 스레드가 동일한 successCont, failCount를 사용하게 되므로, 정합성 보장을 위해 Atomic 타입으로 지정해야 한다.
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 1000개의 결제 요청 작업을 스레드 풀에 제출
        for (int i = 0; i < threadCount; i++) {
            final User user = testUsers.get(i);

            // submit(): 괄호 안의 작업을 스레드 풀의 대기 큐에 등록
            executorService.submit(() -> {
                try {
                    BookingPaymentRequest request = createPaymentRequest(user.getId(), savedAccommodation.getId(), "Y_PAY", "Y", 50000);
                    bookingFacade.checkoutAndPay(request);

                    // 결제 로직이 예외 없이 정상 처리되면 성공 카운트 1 증가
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 비관적 락 대기 타임아웃, 재고 부족 등으로 예외 발생 시 실패 카운트 1 증가
                    failCount.incrementAndGet();
                } finally {
                    // 작업의 성공/실패 여부와 관계없이 래치의 카운트를 1 차감
                    latch.countDown();
                }
            });
        }

        // 스레드 풀에 제출된 1000개의 작업이 모두 처리되어 래치 카운트가 0이 될 때까지 메인 스레드 실행 중지(대기)
        latch.await();

        // 검증 1: 1000건의 요청 중 재고 수량(100개)만큼의 결제만 정확히 성공했는지 확인 (미달 판매 검증)
        assertThat(successCount.get()).isEqualTo(100);

        // 검증 2: 나머지 900건의 요청은 초과 판매되지 않고 정확히 실패 처리되었는지 확인 (초과 판매 검증)
        assertThat(failCount.get()).isEqualTo(900);

        // 검증 3: DB의 실제 숙소 재고가 정확히 0으로 차감되었는지 확인 (데이터 정합성 검증)
        AccommodationStock finalStock = stockRepository.findByAccommodationId(savedAccommodation.getId()).orElseThrow();
        assertThat(finalStock.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("멱등성 방지 테스트: 1명의 유저가 동시에 10번의 결제 요청을 보내면 1번만 성공하고 9번은 실패해야 한다.")
    void testIdempotencyAgainstDoubleSubmit() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        User singleUser = testUsers.get(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    BookingPaymentRequest request = createPaymentRequest(singleUser.getId(), savedAccommodation.getId(), "Y_PAY", "Y", 50000);
                    bookingFacade.checkoutAndPay(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
    }

    // ===================================================================================
    // 테스트 전용 팩토리 메서드 (프로덕션 코드 수정 없이 객체를 생성하기 위한 리플렉션 유틸)
    // ===================================================================================

    // 리플렉션(Reflection)을 활용하여 캡슐화를 훼손하지 않고 테스트용 인스턴스를 생성
    private <T> T createInstance(Class<T> clazz) throws Exception {
        // 1. 클래스(설계도) 정보로부터 접근 제어자와 무관하게 선언된 기본 생성자를 가져온다.
        Constructor<T> constructor = clazz.getDeclaredConstructor();

        // 2. 접근 제어자(private 또는 protected)로 막혀있는 생성자의 접근 권한을 강제로 개방한다.
        constructor.setAccessible(true);

        // 3. 개방된 생성자를 통해 새로운 객체 인스턴스를 생성하고 반환한다.
        return constructor.newInstance();
    }

    private BookingPaymentRequest createPaymentRequest(Long userId, Long accommodationId, String paymentMethod, String provider, int amount) throws Exception {
        BookingPaymentRequest request = createInstance(BookingPaymentRequest.class);
        ReflectionTestUtils.setField(request, "userId", userId);
        ReflectionTestUtils.setField(request, "accommodationId", accommodationId);

        PaymentRequest.PaymentDetail detail = createInstance(PaymentRequest.PaymentDetail.class);
        ReflectionTestUtils.setField(detail, "paymentMethod", paymentMethod);
        ReflectionTestUtils.setField(detail, "provider", provider);
        ReflectionTestUtils.setField(detail, "amount", amount);

        ReflectionTestUtils.setField(request, "payMethods", List.of(detail));
        return request;
    }
}