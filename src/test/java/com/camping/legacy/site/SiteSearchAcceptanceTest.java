package com.camping.legacy.site;

import com.camping.legacy.common.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.camping.legacy.step.ReservationStep.*;
import static com.camping.legacy.step.SiteStep.searchSites;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사이트 검색")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SiteSearchAcceptanceTest extends AcceptanceTest {

    private static final String SITE_A1 = "A-1";
    private static final String CUSTOMER_NAME = "홍길동";
    private static final String PHONE_NUMBER = "010-1234-5678";

    @Test
    void 전체_기간_예약_가능한_사이트_조회() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(10);

        var response = searchSites(startDate, endDate, null);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("siteNumber", String.class)).hasSize(6);
    }

    @Test
    void 중간_날짜에_예약이_있는_사이트는_제외() {
        var reservationStart = LocalDate.now().plusDays(8);
        var reservationEnd = LocalDate.now().plusDays(10);
        createReservation(CUSTOMER_NAME, reservationStart, reservationEnd, SITE_A1, PHONE_NUMBER);

        var searchStart = LocalDate.now().plusDays(6);
        var searchEnd = LocalDate.now().plusDays(11);
        var response = searchSites(searchStart, searchEnd, null);

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> siteNumbers = response.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).doesNotContain(SITE_A1);
    }

    @Test
    void 대형_사이트만_필터링() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(10);

        var response = searchSites(startDate, endDate, "large");

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> siteNumbers = response.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).allMatch(sn -> sn.startsWith("A"));
    }

    @Test
    void 과거_날짜로_검색하면_실패() {
        var pastDate = LocalDate.now().minusDays(1);
        var endDate = LocalDate.now().plusDays(5);

        var response = searchSites(pastDate, endDate, null);

        assertThat(response.statusCode()).isIn(400, 500);
    }

    @Test
    void 취소된_예약이_있는_사이트는_가용으로_표시() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        var createResponse = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);

        var reservationId = createResponse.jsonPath().getLong("id");
        var confirmationCode = createResponse.jsonPath().getString("confirmationCode");
        cancelReservation(reservationId, confirmationCode);

        var response = searchSites(startDate, endDate, null);

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> siteNumbers = response.jsonPath().getList("siteNumber", String.class);
        assertThat(siteNumbers).contains(SITE_A1);
    }
}
