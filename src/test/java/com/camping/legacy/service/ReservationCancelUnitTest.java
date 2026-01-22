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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.camping.legacy.fixture.CampsiteTestBuilder.aLargeSite;
import static com.camping.legacy.fixture.ReservationTestBuilder.aReservation;
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
        testCampsite = aLargeSite().build();
        testReservation = aReservation()
                .withCampsite(testCampsite)
                .build();
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

        @ParameterizedTest(name = "확인코드가 \"{0}\"이면 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"WRONG1"})
        void 확인코드가_유효하지_않으면_예외(String confirmationCode) {
            givenReservationExists(testReservation);

            assertThatThrownBy(() -> reservationService.cancelReservation(1L, confirmationCode))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("확인 코드가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("취소 상태 검증")
    class CancellationStatusValidation {

        @Test
        void 사전_취소_시_CANCELLED_상태() {
            Reservation reservation = aReservation()
                    .withCampsite(testCampsite)
                    .withStartDate(LocalDate.now().plusDays(7))
                    .build();
            givenReservationExists(reservation);

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        void 당일_취소_시_CANCELLED_SAME_DAY_상태() {
            Reservation reservation = aReservation()
                    .withCampsite(testCampsite)
                    .startingToday()
                    .build();
            givenReservationExists(reservation);

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(reservation.getStatus()).isEqualTo("CANCELLED_SAME_DAY");
        }

        @Test
        void 이미_취소된_예약도_재취소_가능_멱등성() {
            Reservation reservation = aReservation()
                    .withCampsite(testCampsite)
                    .cancelled()
                    .build();
            givenReservationExists(reservation);

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        }
    }

    @Nested
    @DisplayName("날짜 경계값 테스트")
    class DateBoundaryTest {

        @Test
        void 시작일_하루_전_취소는_CANCELLED() {
            Reservation reservation = aReservation()
                    .withCampsite(testCampsite)
                    .startingTomorrow()
                    .build();
            givenReservationExists(reservation);

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        }

        @Test
        void 과거_시작일_예약_취소도_가능() {
            Reservation reservation = aReservation()
                    .withCampsite(testCampsite)
                    .withStartDate(LocalDate.now().minusDays(1))
                    .build();
            givenReservationExists(reservation);

            reservationService.cancelReservation(1L, "ABC123");

            assertThat(reservation.getStatus()).isEqualTo("CANCELLED");
        }
    }

    // === Helper Methods ===

    private void givenReservationExists(Reservation reservation) {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
    }
}
