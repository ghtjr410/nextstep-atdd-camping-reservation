package com.camping.legacy.service;

import com.camping.legacy.domain.Campsite;
import com.camping.legacy.domain.Reservation;
import com.camping.legacy.repository.CampsiteRepository;
import com.camping.legacy.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("예약 취소 단위 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationCancelUnitTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CampsiteRepository campsiteRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation testReservation;
    private Campsite testCampsite;

    @BeforeEach
    void setUp() {
        testCampsite = new Campsite();
        testCampsite.setId(1L);
        testCampsite.setSiteNumber("A-1");

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setConfirmationCode("ABC123");
        testReservation.setStartDate(LocalDate.now().plusDays(7));
        testReservation.setEndDate(LocalDate.now().plusDays(9));
        testReservation.setStatus("CONFIRMED");
        testReservation.setCampsite(testCampsite);
    }

    @Nested
    @DisplayName("예약 ID 검증")
    class ReservationIdValidation {

        @Test
        void 존재하지_않는_예약_ID면_예외() {
            given(reservationRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.cancelReservation(999L, "ABC123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("확인 코드 검증")
    class ConfirmationCodeValidation {

        @Test
        void 확인코드가_일치하지_않으면_예외() {
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(1L, "WRONG1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("확인 코드가 일치하지 않습니다.");
        }

        @Test
        void 확인코드가_null이면_예외() {
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            // String.equals(null)은 false를 반환하므로 "확인 코드가 일치하지 않습니다." 예외 발생
            assertThatThrownBy(() -> reservationService.cancelReservation(1L, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("확인 코드가 일치하지 않습니다.");
        }

        @Test
        void 확인코드가_빈문자열이면_예외() {
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            assertThatThrownBy(() -> reservationService.cancelReservation(1L, ""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("확인 코드가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("취소 상태 검증")
    class CancellationStatusValidation {

        @Test
        void 사전_취소_시_CANCELLED_상태() {
            testReservation.setStartDate(LocalDate.now().plusDays(7));
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(testReservation.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        void 당일_취소_시_CANCELLED_SAME_DAY_상태() {
            testReservation.setStartDate(LocalDate.now());
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(testReservation.getStatus()).isEqualTo("CANCELLED_SAME_DAY");
        }

        @Test
        void 이미_취소된_예약도_재취소_가능_멱등성() {
            testReservation.setStatus("CANCELLED");
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            reservationService.cancelReservation(1L, "ABC123");

            // 멱등성: 이미 취소된 예약도 다시 취소 처리됨
            assertThat(testReservation.getStatus()).isEqualTo("CANCELLED");
        }
    }

    @Nested
    @DisplayName("날짜 경계값 테스트")
    class DateBoundaryTest {

        @Test
        void 시작일_하루_전_취소는_CANCELLED() {
            testReservation.setStartDate(LocalDate.now().plusDays(1));
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(testReservation.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        void 과거_시작일_예약_취소도_가능() {
            // 이미 시작된 예약도 취소 가능 (비즈니스 규칙에 따라 다를 수 있음)
            testReservation.setStartDate(LocalDate.now().minusDays(1));
            given(reservationRepository.findById(1L)).willReturn(Optional.of(testReservation));

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(testReservation.getStatus()).isEqualTo("CANCELLED");
        }
    }
}
