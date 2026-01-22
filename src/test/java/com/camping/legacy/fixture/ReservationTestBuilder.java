package com.camping.legacy.fixture;

import com.camping.legacy.domain.Campsite;
import com.camping.legacy.domain.Reservation;

import java.time.LocalDate;

/**
 * Reservation 테스트 데이터 빌더
 */
public class ReservationTestBuilder {

    private Long id = 1L;
    private String customerName = "홍길동";
    private String phoneNumber = "010-1234-5678";
    private LocalDate startDate = LocalDate.now().plusDays(7);
    private LocalDate endDate = LocalDate.now().plusDays(9);
    private String status = "CONFIRMED";
    private String confirmationCode = "ABC123";
    private Campsite campsite;

    public static ReservationTestBuilder aReservation() {
        return new ReservationTestBuilder();
    }

    public ReservationTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ReservationTestBuilder withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public ReservationTestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public ReservationTestBuilder withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ReservationTestBuilder withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public ReservationTestBuilder withDates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public ReservationTestBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public ReservationTestBuilder withConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
        return this;
    }

    public ReservationTestBuilder withCampsite(Campsite campsite) {
        this.campsite = campsite;
        return this;
    }

    public ReservationTestBuilder cancelled() {
        this.status = "CANCELLED";
        return this;
    }

    public ReservationTestBuilder cancelledSameDay() {
        this.status = "CANCELLED_SAME_DAY";
        return this;
    }

    public ReservationTestBuilder startingToday() {
        this.startDate = LocalDate.now();
        return this;
    }

    public ReservationTestBuilder startingTomorrow() {
        this.startDate = LocalDate.now().plusDays(1);
        return this;
    }

    public Reservation build() {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setCustomerName(customerName);
        reservation.setPhoneNumber(phoneNumber);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(status);
        reservation.setConfirmationCode(confirmationCode);
        reservation.setCampsite(campsite);
        return reservation;
    }
}
