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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        @Test
        void 시작일이_null이면_예외() {
            SiteSearchRequest request = createRequest(null, LocalDate.now().plusDays(5), null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("검색 기간을 선택해주세요.");
        }

        @Test
        void 종료일이_null이면_예외() {
            SiteSearchRequest request = createRequest(LocalDate.now().plusDays(1), null, null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("검색 기간을 선택해주세요.");
        }

        @Test
        void 시작일과_종료일_모두_null이면_예외() {
            SiteSearchRequest request = createRequest(null, null, null);

            assertThatThrownBy(() -> siteService.searchAvailableSites(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("검색 기간을 선택해주세요.");
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
    }

    // Helper method
    private SiteSearchRequest createRequest(LocalDate startDate, LocalDate endDate, String size) {
        return new SiteSearchRequest(startDate, endDate, size);
    }
}
