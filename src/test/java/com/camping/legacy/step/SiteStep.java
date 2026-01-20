package com.camping.legacy.step;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDate;

public class SiteStep {

    public static ExtractableResponse<Response> searchSites(LocalDate startDate, LocalDate endDate, String size) {
        RequestSpecification request = RestAssured.given()
                .log().all()
                .queryParam("startDate", startDate.toString())
                .queryParam("endDate", endDate.toString());

        if (size != null) {
            request.queryParam("size", size);
        }

        return request
            .when()
                .get("/api/sites/search")
            .then()
                .log().all()
                .extract();
    }
}
