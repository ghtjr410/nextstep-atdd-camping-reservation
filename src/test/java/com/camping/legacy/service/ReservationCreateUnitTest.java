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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

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
        testCampsite = new Campsite();
        testCampsite.setId(1L);
        testCampsite.setSiteNumber("A-1");
        testCampsite.setMaxPeople(6);
    }

    @Nested
    @DisplayName("사이트 번호 검증")
    class SiteNumberValidation {

        @Test
        void 사이트_번호가_null이면_예외() {
            ReservationRequest request = createRequest(null, "홍길동", "010-1234-5678");

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("사이트 번호를 입력해주세요.");
        }

        @Test
        void 사이트_번호가_빈문자열이면_예외() {
            ReservationRequest request = createRequest("", "홍길동", "010-1234-5678");

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("사이트 번호를 입력해주세요.");
        }

        @Test
        void 사이트_번호가_공백이면_예외() {
            ReservationRequest request = createRequest("   ", "홍길동", "010-1234-5678");

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("사이트 번호를 입력해주세요.");
        }

        @Test
        void 존재하지_않는_사이트면_예외() {
            ReservationRequest request = createRequest("Z-999", "홍길동", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("Z-999")).willReturn(Optional.empty());

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
            ReservationRequest request = createRequestWithDates("A-1", "홍길동", "010-1234-5678", null, LocalDate.now().plusDays(5));
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약 기간을 선택해주세요.");
        }

        @Test
        void 종료일이_null이면_예외() {
            ReservationRequest request = createRequestWithDates("A-1", "홍길동", "010-1234-5678", LocalDate.now().plusDays(1), null);
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약 기간을 선택해주세요.");
        }
    }

    @Nested
    @DisplayName("고객명 검증")
    class CustomerNameValidation {

        @Test
        void 고객명이_null이면_예외() {
            ReservationRequest request = createRequest("A-1", null, "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름을 입력해주세요.");
        }

        @Test
        void 고객명이_빈문자열이면_예외() {
            ReservationRequest request = createRequest("A-1", "", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름을 입력해주세요.");
        }

        @Test
        void 고객명이_1자이면_예외() {
            ReservationRequest request = createRequest("A-1", "홍", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름은 최소 2자 이상이어야 합니다.");
        }

        @Test
        void 고객명이_21자이상이면_예외() {
            ReservationRequest request = createRequest("A-1", "가나다라마바사아자차카타파하가나다라마바사", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("예약자 이름은 최대 20자까지 가능합니다.");
        }

        @Test
        void 고객명이_경계값_2자이면_통과() {
            ReservationRequest request = createRequest("A-1", "홍길", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));
            given(reservationRepository.hasOverlappingReservation(
                    any(), any(), any(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // 예외가 발생하지 않으면 통과
            reservationService.createReservation(request);
        }

        @Test
        void 고객명이_경계값_20자이면_통과() {
            ReservationRequest request = createRequest("A-1", "가나다라마바사아자차카타파하가나다라마바", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));
            given(reservationRepository.hasOverlappingReservation(
                    any(), any(), any(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            reservationService.createReservation(request);
        }
    }

    @Nested
    @DisplayName("전화번호 검증")
    class PhoneNumberValidation {

        @Test
        void 전화번호가_9자리이면_예외() {
            ReservationRequest request = createRequest("A-1", "홍길동", "010-1234-56");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("전화번호 형식이 올바르지 않습니다.");
        }

        @Test
        void 전화번호가_12자리이면_예외() {
            ReservationRequest request = createRequest("A-1", "홍길동", "010-1234-56789");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("전화번호 형식이 올바르지 않습니다.");
        }

        @Test
        void 전화번호에_문자가_포함되면_예외() {
            ReservationRequest request = createRequest("A-1", "홍길동", "010-abcd-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("전화번호는 숫자만 입력 가능합니다.");
        }

        @Test
        void 전화번호가_null이면_통과() {
            ReservationRequest request = createRequest("A-1", "홍길동", null);
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));
            given(reservationRepository.hasOverlappingReservation(
                    any(), any(), any(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            reservationService.createReservation(request);
        }

        @Test
        void 전화번호가_경계값_10자리이면_통과() {
            ReservationRequest request = createRequest("A-1", "홍길동", "010-123-4567");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));
            given(reservationRepository.hasOverlappingReservation(
                    any(), any(), any(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            reservationService.createReservation(request);
        }

        @Test
        void 전화번호가_경계값_11자리이면_통과() {
            ReservationRequest request = createRequest("A-1", "홍길동", "010-1234-5678");
            given(campsiteRepository.findBySiteNumber("A-1")).willReturn(Optional.of(testCampsite));
            given(reservationRepository.hasOverlappingReservation(
                    any(), any(), any(), any())).willReturn(false);
            given(reservationRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            reservationService.createReservation(request);
        }
    }

    // Helper methods
    private ReservationRequest createRequest(String siteNumber, String customerName, String phoneNumber) {
        return createRequestWithDates(siteNumber, customerName, phoneNumber,
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(9));
    }

    private ReservationRequest createRequestWithDates(String siteNumber, String customerName, String phoneNumber,
                                                       LocalDate startDate, LocalDate endDate) {
        ReservationRequest request = new ReservationRequest();
        request.setSiteNumber(siteNumber);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }
}
