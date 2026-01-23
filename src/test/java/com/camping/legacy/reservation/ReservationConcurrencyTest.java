package com.camping.legacy.reservation;

import com.camping.legacy.domain.Campsite;
import com.camping.legacy.dto.ReservationRequest;
import com.camping.legacy.repository.CampsiteRepository;
import com.camping.legacy.repository.ReservationRepository;
import com.camping.legacy.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CampsiteRepository campsiteRepository;

    private Campsite testCampsite;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        testCampsite = campsiteRepository.findBySiteNumber("A-1")
                .orElseGet(() -> {
                    Campsite campsite = new Campsite();
                    campsite.setSiteNumber("A-1");
                    campsite.setMaxPeople(6);
                    return campsiteRepository.save(campsite);
                });
    }

    @Test
    void 동일_사이트에_동시_예약_요청_시_1건만_성공해야_한다() throws InterruptedException {
        // given
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(12);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    ReservationRequest request = new ReservationRequest();
                    request.setSiteNumber("A-1");
                    request.setCustomerName("고객" + index);
                    request.setPhoneNumber("010-1234-567" + index);
                    request.setStartDate(startDate);
                    request.setEndDate(endDate);

                    reservationService.createReservation(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long reservationCount = reservationRepository.count();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("실제 예약 건수: " + reservationCount);

        // 기대: 1건만 성공, 1건 실패
        // 현실(버그): 2건 모두 성공 가능
        assertThat(successCount.get())
                .as("동시 요청 시 1건만 성공해야 함 (현재 버그로 인해 2건 성공)")
                .isEqualTo(1);
    }
}
