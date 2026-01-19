package com.camping.legacy.reservation;

import com.camping.legacy.common.AcceptanceTest;
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
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        var createResponse = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
        var reservationId = createResponse.jsonPath().getLong("id");
        var confirmationCode = createResponse.jsonPath().getString("confirmationCode");

        cancelReservation(reservationId, confirmationCode);

        var response = getReservation(reservationId);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CANCELLED");
    }

    @Test
    void 잘못된_확인코드로_취소하면_실패() {
        var startDate = LocalDate.now().plusDays(7);
        var endDate = LocalDate.now().plusDays(9);
        var createResponse = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
        var reservationId = createResponse.jsonPath().getLong("id");
        var wrongCode = "WRONG1";

        var response = cancelReservation(reservationId, wrongCode);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("message")).isNotEmpty();
    }

    @Test
    void 당일_취소_시_환불_불가_처리() {
        var startDate = LocalDate.now();
        var endDate = LocalDate.now().plusDays(2);
        var createResponse = createReservation(CUSTOMER_NAME, startDate, endDate, SITE_A1, PHONE_NUMBER);
        var reservationId = createResponse.jsonPath().getLong("id");
        var confirmationCode = createResponse.jsonPath().getString("confirmationCode");

        cancelReservation(reservationId, confirmationCode);

        var response = getReservation(reservationId);
        assertThat(response.jsonPath().getString("status")).isEqualTo("CANCELLED_SAME_DAY");
    }
}
