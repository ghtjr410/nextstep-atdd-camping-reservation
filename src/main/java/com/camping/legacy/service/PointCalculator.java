package com.camping.legacy.service;

import com.camping.legacy.domain.Reservation;
import com.camping.legacy.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 포인트 적립 계산기
 * - 기본: 결제 금액의 5% 적립
 * - 주말 포함: 10% 적립
 * - 성수기(주말 미포함): 3% 적립
 */
@Component
public class PointCalculator {

    private static final double RATE_DEFAULT = 0.05;      // 기본 5%
    private static final double RATE_WEEKEND = 0.10;      // 주말 10%
    private static final double RATE_PEAK_SEASON = 0.03;  // 성수기 3%

    private final PriceCalculator priceCalculator;

    public PointCalculator(PriceCalculator priceCalculator) {
        this.priceCalculator = priceCalculator;
    }

    /**
     * 포인트 계산 (가격 기반)
     */
    public int calculate(LocalDate startDate, LocalDate endDate, int totalPrice) {
        double pointRate = determinePointRate(startDate, endDate);
        return (int) (totalPrice * pointRate);
    }

    /**
     * 포인트 계산 (기간과 사이트 번호 기반 - 가격 자동 계산)
     */
    public int calculate(LocalDate startDate, LocalDate endDate, String siteNumber) {
        int totalPrice = priceCalculator.calculate(startDate, endDate, siteNumber);
        return calculate(startDate, endDate, totalPrice);
    }

    /**
     * Reservation 객체로 포인트 계산
     */
    public int calculate(Reservation reservation) {
        int totalPrice = priceCalculator.calculate(reservation);
        return calculate(reservation.getStartDate(), reservation.getEndDate(), totalPrice);
    }

    /**
     * 포인트 적립률 결정
     */
    public double determinePointRate(LocalDate startDate, LocalDate endDate) {
        boolean hasWeekend = hasWeekendInPeriod(startDate, endDate);
        boolean isPeakSeason = DateUtils.isPeakSeason(startDate);

        if (hasWeekend) {
            return RATE_WEEKEND;
        } else if (isPeakSeason) {
            return RATE_PEAK_SEASON;
        }
        return RATE_DEFAULT;
    }

    /**
     * 기간 내 주말 포함 여부 확인
     */
    public boolean hasWeekendInPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (DateUtils.isWeekend(current)) {
                return true;
            }
            current = current.plusDays(1);
        }
        return false;
    }
}
