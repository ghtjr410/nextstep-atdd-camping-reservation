package com.camping.legacy.service;

import com.camping.legacy.domain.Campsite;
import com.camping.legacy.dto.ReservationRequest;
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
import static com.camping.legacy.fixture.ReservationRequestTestBuilder.aReservationRequest;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("예약 생성 단위 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationCreateUnitTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CampsiteRepository campsiteRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Campsite testCampsite;

    @BeforeEach
    void setUp() {
        testCampsite = aLargeSite().build();
    }

    @Nested
    @DisplayName("사이트 번호 검증")
    class SiteNumberValidation {

        @ParameterizedTest(name = "사이트 번호가 \"{0}\"이면 예외")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 사이트_번호가_null_또는_빈값이면_예외(String siteNumber) {
            ReservationRequest request = aReservationRequest()
                    .withSiteNumber(siteNumber)
                    .build();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("사이트 번호를 입력해주세요.");
        }

        @Test
        void 존재하지_않는_사이트면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withSiteNumber("Z-999")
                    .build();
            given(campsiteRepository.findBySiteNumberWithLock("Z-999")).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("존재하지 않는 캠핑장입니다.");
        }
    }

    @Nested
    @DisplayName("날짜 검증")
    class DateValidation {

        @Test
        void 시작일이_null이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withStartDate(null)
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약 기간을 선택해주세요.");
        }

        @Test
        void 종료일이_null이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withEndDate(null)
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약 기간을 선택해주세요.");
        }

        @Test
        void 종료일이_시작일보다_이전이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withDates(LocalDate.now().plusDays(10), LocalDate.now().plusDays(5))
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("종료일이 시작일보다 이전일 수 없습니다.");
        }

        @Test
        void 시작일이_과거이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withDates(LocalDate.now().minusDays(1), LocalDate.now().plusDays(5))
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("과거 날짜로 예약할 수 없습니다.");
        }

        @Test
        void 예약기간이_30일이면_통과() {
            LocalDate startDate = LocalDate.now().plusDays(1);
            ReservationRequest request = aReservationRequest()
                    .withDates(startDate, startDate.plusDays(30))
                    .build();
            givenCampsiteExists();
            givenNoConflictingReservation();
            givenReservationSaveSucceeds();

            assertThatCode(() -> reservationService.createReservation(request))
                    .doesNotThrowAnyException();
        }

        @Test
        void 예약기간이_31일이면_예외() {
            LocalDate startDate = LocalDate.now().plusDays(1);
            ReservationRequest request = aReservationRequest()
                    .withDates(startDate, startDate.plusDays(31))
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약 기간은 최대 30일입니다.");
        }
    }

    @Nested
    @DisplayName("고객명 검증")
    class CustomerNameValidation {

        @ParameterizedTest(name = "고객명이 \"{0}\"이면 예외")
        @NullAndEmptySource
        void 고객명이_null_또는_빈값이면_예외(String customerName) {
            ReservationRequest request = aReservationRequest()
                    .withCustomerName(customerName)
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름을 입력해주세요.");
        }

        @Test
        void 고객명이_1자이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withCustomerName("홍")
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름은 최소 2자 이상이어야 합니다.");
        }

        @Test
        void 고객명이_21자이상이면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withCustomerName("가나다라마바사아자차카타파하가나다라마바사")
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름은 최대 20자까지 가능합니다.");
        }

        @ParameterizedTest(name = "고객명이 \"{0}\"이면 통과 (경계값)")
        @ValueSource(strings = {"홍길", "가나다라마바사아자차카타파하가나다라마바"})
        void 고객명이_경계값이면_통과(String customerName) {
            ReservationRequest request = aReservationRequest()
                    .withCustomerName(customerName)
                    .build();
            givenCampsiteExists();
            givenNoConflictingReservation();
            givenReservationSaveSucceeds();

            assertThatCode(() -> reservationService.createReservation(request))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("전화번호 검증")
    class PhoneNumberValidation {

        @ParameterizedTest(name = "전화번호 \"{0}\"는 길이 오류")
        @ValueSource(strings = {"010-1234-56", "010-1234-56789"})
        void 전화번호_길이가_유효하지_않으면_예외(String phoneNumber) {
            ReservationRequest request = aReservationRequest()
                    .withPhoneNumber(phoneNumber)
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("전화번호 형식이 올바르지 않습니다.");
        }

        @Test
        void 전화번호에_문자가_포함되면_예외() {
            ReservationRequest request = aReservationRequest()
                    .withPhoneNumber("010-abcd-5678")
                    .build();
            givenCampsiteExists();

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("전화번호는 숫자만 입력 가능합니다.");
        }

        @Test
        void 전화번호가_null이면_통과() {
            ReservationRequest request = aReservationRequest()
                    .withPhoneNumber(null)
                    .build();
            givenCampsiteExists();
            givenNoConflictingReservation();
            givenReservationSaveSucceeds();

            assertThatCode(() -> reservationService.createReservation(request))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "전화번호 \"{0}\"는 유효 (경계값)")
        @ValueSource(strings = {"010-123-4567", "010-1234-5678"})
        void 전화번호가_경계값이면_통과(String phoneNumber) {
            ReservationRequest request = aReservationRequest()
                    .withPhoneNumber(phoneNumber)
                    .build();
            givenCampsiteExists();
            givenNoConflictingReservation();
            givenReservationSaveSucceeds();

            assertThatCode(() -> reservationService.createReservation(request))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("중복 예약 검증")
    class DuplicateReservationValidation {

        @Test
        void 해당_기간에_이미_예약이_존재하면_예외() {
            ReservationRequest request = aReservationRequest().build();
            givenCampsiteExists();
            given(reservationRepository.hasOverlappingReservation(any(), any(), any(), any())).willReturn(true);

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("해당 기간에 이미 예약이 존재합니다.");
        }
    }

    // === Helper Methods ===

    private void givenCampsiteExists() {
        given(campsiteRepository.findBySiteNumberWithLock("A-1")).willReturn(Optional.of(testCampsite));
    }

    private void givenNoConflictingReservation() {
        given(reservationRepository.hasOverlappingReservation(any(), any(), any(), any())).willReturn(false);
    }

    private void givenReservationSaveSucceeds() {
        given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
    }
}
