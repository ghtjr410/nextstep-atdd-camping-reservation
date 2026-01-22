package com.camping.legacy.service;

import com.camping.legacy.domain.Reservation;
import com.camping.legacy.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 예약 가격 계산기
 * - 기본 가격: 대형(A) 80,000원, 소형(B) 50,000원, 기타 60,000원 (1박 기준)
 * - 주말 할증: 30% 추가
 * - 성수기 할증: 50% 추가 (7-8월)
 * - 성수기 주말: 70% 추가
 */
@Component
public class PriceCalculator {

    private static final int PRICE_LARGE = 80000;   // 대형 (A)
    private static final int PRICE_SMALL = 50000;   // 소형 (B)
    private static final int PRICE_DEFAULT = 60000; // 기타

    private static final double SURCHARGE_WEEKEND = 1.3;           // 주말 30%
    private static final double SURCHARGE_PEAK_SEASON = 1.5;       // 성수기 50%
    private static final double SURCHARGE_PEAK_WEEKEND = 1.7;      // 성수기 주말 70%

    /**
     * 예약 가격 계산
     */
    public int calculate(LocalDate startDate, LocalDate endDate, String siteNumber) {
        int totalPrice = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            int dailyPrice = calculateDailyPrice(current, siteNumber);
            totalPrice += dailyPrice;
            current = current.plusDays(1);
        }

        return totalPrice;
    }

    /**
     * Reservation 객체로 가격 계산
     */
    public int calculate(Reservation reservation) {
        return calculate(
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getCampsite().getSiteNumber()
        );
    }

    /**
     * 일별 가격 계산 (기본가 + 할증)
     */
    public int calculateDailyPrice(LocalDate date, String siteNumber) {
        int basePrice = getBasePrice(siteNumber);
        return applyPriceSurcharge(basePrice, date);
    }

    /**
     * 사이트 종류별 기본 가격
     */
    public int getBasePrice(String siteNumber) {
        if (siteNumber.startsWith("A")) {
            return PRICE_LARGE;
        } else if (siteNumber.startsWith("B")) {
            return PRICE_SMALL;
        }
        return PRICE_DEFAULT;
    }

    /**
     * 할증 적용
     */
    public int applyPriceSurcharge(int basePrice, LocalDate date) {
        boolean isWeekend = DateUtils.isWeekend(date);
        boolean isPeakSeason = DateUtils.isPeakSeason(date);

        if (isWeekend && isPeakSeason) {
            return (int) (basePrice * SURCHARGE_PEAK_WEEKEND);
        } else if (isPeakSeason) {
            return (int) (basePrice * SURCHARGE_PEAK_SEASON);
        } else if (isWeekend) {
            return (int) (basePrice * SURCHARGE_WEEKEND);
        }
        return basePrice;
    }
}
