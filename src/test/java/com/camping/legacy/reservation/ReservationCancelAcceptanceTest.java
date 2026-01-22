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

@DisplayName("예약 취소")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationCancelAcceptanceTest extends AcceptanceTest {

    private static final String SITE_A1 = "A-1";
    private static final String CUSTOMER_NAME = "홍길동";
    private static final String PHONE_NUMBER = "010-1234-5678";

    @Test
    void 올바른_확인코드로_사전_취소() {
        var 예약 = 일주일_후_예약을_생성한다();

        예약을_취소한다(예약);

        예약_상태가_CANCELLED인지_확인한다(예약);
    }

    @Test
    void 잘못된_확인코드로_취소하면_실패() {
        var 예약 = 일주일_후_예약을_생성한다();

        var 응답 = 잘못된_확인코드로_예약을_취소한다(예약);

        취소_요청이_실패했는지_확인한다(응답);
    }

    @Test
    void 당일_취소_시_환불_불가_처리() {
        var 예약 = 당일_시작_예약을_생성한다();

        예약을_취소한다(예약);

        예약_상태가_CANCELLED_SAME_DAY인지_확인한다(예약);
    }

    // Given
    private ExtractableResponse<Response> 일주일_후_예약을_생성한다() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        return createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    private ExtractableResponse<Response> 당일_시작_예약을_생성한다() {
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusDays(2);
        return createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
    }

    // When
    private void 예약을_취소한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        var confirmationCode = 예약.jsonPath().getString("confirmationCode");
        cancelReservation(reservationId, confirmationCode);
    }

    private ExtractableResponse<Response> 잘못된_확인코드로_예약을_취소한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        return cancelReservation(reservationId, "WRONG1");
    }

    // Then
    private void 예약_상태가_CANCELLED인지_확인한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        var response = getReservation(reservationId);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CANCELLED");
    }

    private void 예약_상태가_CANCELLED_SAME_DAY인지_확인한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        var response = getReservation(reservationId);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CANCELLED_SAME_DAY");
    }

    private void 취소_요청이_실패했는지_확인한다(ExtractableResponse<Response> 응답) {
        assertThat(응답.statusCode()).isEqualTo(400);
        assertThat(응답.jsonPath().getString("message")).isNotEmpty();
    }
}
