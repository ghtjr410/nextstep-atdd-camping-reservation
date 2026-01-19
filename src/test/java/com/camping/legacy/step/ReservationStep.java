package com.camping.legacy.step;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

public class ReservationStep {

    public static ExtractableResponse<Response> createReservation(String customerName, LocalDate startDate, LocalDate endDate, String siteNumber, String phoneNumber) {
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

    public static ExtractableResponse<Response> cancelReservation(Long reservationId, String confirmationCode) {
        return RestAssured.given()
                .log().all()
                .queryParam("confirmationCode", confirmationCode)
            .when()
                .delete("/api/reservations/{id}", reservationId)
            .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> getReservation(Long reservationId) {
        return RestAssured.given()
                .log().all()
            .when()
                .get("/api/reservations/{id}", reservationId)
            .then()
                .log().all()
                .extract();
    }
}
