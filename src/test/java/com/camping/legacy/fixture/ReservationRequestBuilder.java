package com.camping.legacy.fixture;

import com.camping.legacy.dto.ReservationRequest;

import java.time.LocalDate;

/**
 * ReservationRequest 테스트 데이터 빌더
 * 기본값이 설정되어 있어 필요한 필드만 오버라이드하면 됨
 */
public class ReservationRequestBuilder {

    private String siteNumber = "A-1";
    private String customerName = "홍길동";
    private String phoneNumber = "010-1234-5678";
    private LocalDate startDate = LocalDate.now().plusDays(7);
    private LocalDate endDate = LocalDate.now().plusDays(9);

    public static ReservationRequestBuilder aReservationRequest() {
        return new ReservationRequestBuilder();
    }

    public ReservationRequestBuilder withSiteNumber(String siteNumber) {
        this.siteNumber = siteNumber;
        return this;
    }

    public ReservationRequestBuilder withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public ReservationRequestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ReservationRequestBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ReservationRequestBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public ReservationRequestBuilder withDates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public ReservationRequest build() {
        ReservationRequest request = new ReservationRequest();
        request.setSiteNumber(siteNumber);
        request.setCustomerName(customerName);
        request.setPhoneNumber(phoneNumber);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        return request;
    }
}
