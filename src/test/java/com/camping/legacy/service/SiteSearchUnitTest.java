package com.camping.legacy.service;

import com.camping.legacy.dto.SiteSearchRequest;
import com.camping.legacy.repository.CampsiteRepository;
import com.camping.legacy.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("사이트 검색 단위 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SiteSearchUnitTest {

    @Mock
    private CampsiteRepository campsiteRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private SiteService siteService;

    @Nested
    @DisplayName("검색 날짜 검증")
    class SearchDateValidation {

        @ParameterizedTest(name = "시작일={0}, 종료일={1}이면 예외")
        @MethodSource("nullDateProvider")
        void 날짜가_null이면_예외(LocalDate startDate, LocalDate endDate) {
            SiteSearchRequest request = createRequest(startDate, endDate, null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("검색 기간을 선택해주세요.");
        }

        static Stream<Arguments> nullDateProvider() {
            LocalDate validDate = LocalDate.now().plusDays(5);
            return Stream.of(
                    Arguments.of(null, validDate),       // 시작일만 null
                    Arguments.of(validDate, null),       // 종료일만 null
                    Arguments.of(null, null)             // 둘 다 null
            );
        }

        @Test
        void 종료일이_시작일보다_이전이면_예외() {
            SiteSearchRequest request = createRequest(
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(5),
                    null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("종료일이 시작일보다 이전일 수 없습니다.");
        }

        @Test
        void 시작일이_과거이면_예외() {
            SiteSearchRequest request = createRequest(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5),
                    null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("과거 날짜는 검색할 수 없습니다.");
        }

        @Test
        void 시작일과_종료일이_같으면_당일검색_가능() {
            LocalDate sameDay = LocalDate.now().plusDays(5);
            SiteSearchRequest request = createRequest(sameDay, sameDay, null);
            given(campsiteRepository.findAll()).willReturn(java.util.Collections.emptyList());

            assertThatCode(() -> siteService.searchAvailableSites(request))
                    .doesNotThrowAnyException();
        }
    }

    // Helper method
    private SiteSearchRequest createRequest(LocalDate startDate, LocalDate endDate, String size) {
        return new SiteSearchRequest(startDate, endDate, size);
    }
}
