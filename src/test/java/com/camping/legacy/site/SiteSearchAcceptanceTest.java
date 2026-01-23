package com.camping.legacy.site;

import com.camping.legacy.common.AcceptanceTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.camping.legacy.step.ReservationStep.*;
import static com.camping.legacy.step.SiteStep.searchSites;
import static org.assertj.core.api.Assertions.assertThat;

class SiteSearchAcceptanceTest extends AcceptanceTest {

    private static final String SITE_A1 = "A-1";
    private static final String CUSTOMER_NAME = "홍길동";
    private static final String PHONE_NUMBER = "010-1234-5678";

    @Test
    void 전체_기간_예약_가능한_사이트_조회() {
        var 응답 = 일주일_후_기간으로_사이트를_검색한다();

        검색이_성공했는지_확인한다(응답);
        모든_사이트가_조회되는지_확인한다(응답);
    }

    @Test
    void 중간_날짜에_예약이_있는_사이트는_제외() {
        중간_날짜에_A1_사이트를_예약한다();

        var 응답 = 예약_기간을_포함하는_범위로_검색한다();

        검색이_성공했는지_확인한다(응답);
        검색_결과에_A1이_포함되지_않는지_확인한다(응답);
    }

    @Test
    void 대형_사이트만_필터링() {
        var 응답 = 대형_사이트만_검색한다();

        검색이_성공했는지_확인한다(응답);
        모든_결과가_대형_사이트인지_확인한다(응답);
    }

    @Test
    void 과거_날짜로_검색하면_실패() {
        var 응답 = 과거_날짜로_검색한다();

        검색이_실패했는지_확인한다(응답);
    }

    @Test
    void 취소된_예약이_있는_사이트는_가용으로_표시() {
        var 예약 = 일주일_후_A1_사이트를_예약한다();
        예약을_취소한다(예약);

        var 응답 = 같은_기간으로_사이트를_검색한다();

        검색이_성공했는지_확인한다(응답);
        검색_결과에_A1이_포함되는지_확인한다(응답);
    }

    // Given
    private void 중간_날짜에_A1_사이트를_예약한다() {
        var reservationStart = LocalDate.now().plusDays(8);
        var reservationEnd = LocalDate.now().plusDays(10);
        예약을_생성한다(CUSTOMER_NAME, reservationStart, reservationEnd, SITE_A1, PHONE_NUMBER);
    }

    private ExtractableResponse<Response> 일주일_후_A1_사이트를_예약한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        return 예약을_생성한다(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    // When
    private ExtractableResponse<Response> 일주일_후_기간으로_사이트를_검색한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(10);
        return searchSites(startDate, endDate, null);
    }

    private ExtractableResponse<Response> 예약_기간을_포함하는_범위로_검색한다() {
        var searchStart = LocalDate.now().plusDays(6);
        var searchEnd = LocalDate.now().plusDays(11);
        return searchSites(searchStart, searchEnd, null);
    }

    private ExtractableResponse<Response> 대형_사이트만_검색한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(10);
        return searchSites(startDate, endDate, "large");
    }

    private ExtractableResponse<Response> 과거_날짜로_검색한다() {
        var pastDate = LocalDate.now().minusDays(1);
        var endDate = LocalDate.now().plusDays(5);
        return searchSites(pastDate, endDate, null);
    }

    private ExtractableResponse<Response> 같은_기간으로_사이트를_검색한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        return searchSites(startDate, endDate, null);
    }

    // Then
    private void 검색이_성공했는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.statusCode()).isEqualTo(200);
    }

    private void 검색이_실패했는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.statusCode()).isIn(400, 500);
    }

    private void 모든_사이트가_조회되는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.jsonPath().getList("siteNumber", String.class)).hasSize(6);
    }

    private void 검색_결과에_A1이_포함되지_않는지_확인한다(ExtractableResponse<Response> 응답) {
        List<String> siteNumbers = 응답.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).doesNotContain(SITE_A1);
    }

    private void 검색_결과에_A1이_포함되는지_확인한다(ExtractableResponse<Response> 응답) {
        List<String> siteNumbers = 응답.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).contains(SITE_A1);
    }

    private void 모든_결과가_대형_사이트인지_확인한다(ExtractableResponse<Response> 응답) {
        List<String> siteNumbers = 응답.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).allMatch(sn -> sn.startsWith("A"));
    }
}
