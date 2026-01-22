package com.camping.legacy.reservation;

import com.camping.legacy.common.AcceptanceTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
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
        var 응답 = 일주일_후_예약을_생성한다();

        예약이_성공했는지_확인한다(응답);
        확인코드가_발급되었는지_확인한다(응답);
    }

    @Test
    void 과거_날짜로_예약하면_실패() {
        var 응답 = 과거_날짜로_예약을_생성한다();

        예약이_실패했는지_확인한다(응답);
    }

    @Test
    void 종료일이_시작일보다_이전이면_실패() {
        var 응답 = 종료일이_시작일보다_이전인_예약을_생성한다();

        예약이_실패했는지_확인한다(응답);
    }

    @Test
    void 동일_사이트에_겹치는_기간으로_예약하면_실패() {
        일주일_후_예약을_생성한다();

        var 응답 = 겹치는_기간으로_예약을_생성한다();

        예약이_실패했는지_확인한다(응답);
    }

    @Test
    void 취소된_예약이_있는_기간에_새_예약_가능() {
        var 예약 = 일주일_후_예약을_생성한다();
        예약을_취소한다(예약);

        var 응답 = 같은_기간으로_다른_고객이_예약한다();

        예약이_성공했는지_확인한다(응답);
    }

    @Test
    void _30일_초과_예약하면_실패() {
        var 응답 = _30일_초과_예약을_생성한다();

        예약이_실패했는지_확인한다(응답);
    }

    // Given
    private ExtractableResponse<Response> 일주일_후_예약을_생성한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        return createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    private ExtractableResponse<Response> 과거_날짜로_예약을_생성한다() {
        var pastDate = LocalDate.now().minusDays(1);
        var endDate = LocalDate.now().plusDays(1);
        return createReservation(CUSTOMER_NAME, pastDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    private ExtractableResponse<Response> 종료일이_시작일보다_이전인_예약을_생성한다() {
        var startDate = LocalDate.now().plusDays(10);
        var endDate = LocalDate.now().plusDays(5);
        return createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    private ExtractableResponse<Response> 겹치는_기간으로_예약을_생성한다() {
        var overlappingStart = LocalDate.now().plusDays(8);
        var overlappingEnd = LocalDate.now().plusDays(10);
        return createReservation("김철수", overlappingStart, overlappingEnd, SITE_A1, "010-9999-8888");
    }

    private ExtractableResponse<Response> 같은_기간으로_다른_고객이_예약한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        return createReservation("김철수", startDate, endDate, SITE_A1, "010-9999-8888");
    }

    private ExtractableResponse<Response> _30일_초과_예약을_생성한다() {
        var startDate = LocalDate.now().plusDays(1);
        var endDate = LocalDate.now().plusDays(32);
        return createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    // When
    private void 예약을_취소한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        var confirmationCode = 예약.jsonPath().getString("confirmationCode");
        cancelReservation(reservationId, confirmationCode);
    }

    // Then
    private void 예약이_성공했는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.statusCode()).isEqualTo(201);
        assertThat(응답.jsonPath().getString("status")).isEqualTo("CONFIRMED");
    }

    private void 확인코드가_발급되었는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.jsonPath().getString("confirmationCode")).hasSize(6);
    }

    private void 예약이_실패했는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.statusCode()).isEqualTo(409);
    }
}
