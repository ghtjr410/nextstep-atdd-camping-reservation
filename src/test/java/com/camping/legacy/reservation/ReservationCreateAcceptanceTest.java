package com.camping.legacy.reservation;

import com.camping.legacy.common.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.camping.legacy.step.ReservationStep.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("예약 생성")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationCreateAcceptanceTest extends AcceptanceTest {

    private static final String SITE_A1 = "A-1";
    private static final String CUSTOMER_NAME = "홍길동";
    private static final String PHONE_NUMBER = "010-1234-5678";

    @Test
    void 유효한_정보로_예약_생성() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);

        var response = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("confirmationCode")).hasSize(6);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CONFIRMED");
    }

    @Test
    void 과거_날짜로_예약하면_실패() {
        var pastDate = LocalDate.now().minusDays(1);
        var endDate = LocalDate.now().plusDays(1);

        var response = createReservation(CUSTOMER_NAME, pastDate, endDate, SITE_A1, PHONE_NUMBER);

        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    void 종료일이_시작일보다_이전이면_실패() {
        var startDate = LocalDate.now().plusDays(10);
        var endDate = LocalDate.now().plusDays(5);

        var response = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);

        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    void 동일_사이트에_겹치는_기간으로_예약하면_실패() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);

        var overlappingStart = LocalDate.now().plusDays(8);
        var overlappingEnd = LocalDate.now().plusDays(10);
        var response = createReservation("김철수", overlappingStart, overlappingEnd, SITE_A1, "010-9999-8888");

        // then
        assertThat(response.statusCode()).isEqualTo(409);
        assertThat(response.jsonPath().getString("message")).isNotEmpty();
    }

    @Test
    void 취소된_예약이_있는_기간에_새_예약_가능() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        var createResponse = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);

        var reservationId = createResponse.jsonPath().getLong("id");
        var confirmationCode = createResponse.jsonPath().getString("confirmationCode");
        cancelReservation(reservationId, confirmationCode);

        var response = createReservation("김철수", startDate, endDate, SITE_A1, "010-9999-8888");

        assertThat(response.statusCode()).isEqualTo(201);
    }
}
