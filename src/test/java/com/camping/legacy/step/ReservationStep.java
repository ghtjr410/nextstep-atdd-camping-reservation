package com.camping.legacy.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

public class ReservationStep {

    public static ExtractableResponse<Response> 예약을_생성한다(String customerName, LocalDate startDate, LocalDate endDate, String siteNumber, String phoneNumber) {
        return RestAssured.given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "customerName", customerName,
                        "startDate", startDate.toString(),
                        "endDate", endDate.toString(),
                        "siteNumber", siteNumber,
                        "phoneNumber", phoneNumber
                ))
            .when()
                .post("/api/reservations")
            .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 예약을_취소한다(ExtractableResponse<Response> 예약) {
        var reservationId = 예약.jsonPath().getLong("id");
        var confirmationCode = 예약.jsonPath().getString("confirmationCode");
        return 예약을_취소한다(reservationId, confirmationCode);
    }

    public static ExtractableResponse<Response> 예약을_취소한다(ExtractableResponse<Response> 예약, String code) {
        var reservationId = 예약.jsonPath().getLong("id");
        return 예약을_취소한다(reservationId, code);
    }

    private static ExtractableResponse<Response> 예약을_취소한다(Long reservationId, String confirmationCode) {
        return RestAssured.given()
                .log().all()
                .queryParam("confirmationCode", confirmationCode)
            .when()
                .delete("/api/reservations/{id}", reservationId)
            .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 예약을_조회한다(Long reservationId) {
        return RestAssured.given()
                .log().all()
            .when()
                .get("/api/reservations/{id}", reservationId)
            .then()
                .log().all()
                .extract();
    }
}
