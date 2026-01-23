package com.camping.legacy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PointCalculatorTest {

    private PointCalculator pointCalculator;
    private PriceCalculator priceCalculator;

    @BeforeEach
    void setUp() {
        priceCalculator = new PriceCalculator();
        pointCalculator = new PointCalculator(priceCalculator);
    }

    @Nested
    class PointRate {

        @Test
        void 기본_적립률은_5퍼센트() {
            // 비성수기 평일
            LocalDate monday = getWeekday(LocalDate.of(2026, 3, 2));
            LocalDate tuesday = monday.plusDays(1);

            double rate = pointCalculator.determinePointRate(monday, tuesday);

            assertThat(rate).isEqualTo(0.05);
        }

        @Test
        void 주말_포함시_적립률은_10퍼센트() {
            // 금~일 (주말 포함)
            LocalDate friday = LocalDate.of(2026, 3, 6);
            LocalDate sunday = LocalDate.of(2026, 3, 8);

            double rate = pointCalculator.determinePointRate(friday, sunday);

            assertThat(rate).isEqualTo(0.10);
        }

        @Test
        void 성수기_평일만_적립률은_3퍼센트() {
            // 7월 평일만
            LocalDate monday = getWeekday(LocalDate.of(2026, 7, 6));
            LocalDate wednesday = monday.plusDays(2);

            double rate = pointCalculator.determinePointRate(monday, wednesday);

            // 주말 미포함 시 성수기 3%
            assertThat(rate).isEqualTo(0.03);
        }

        @Test
        void 성수기_주말_포함시_적립률은_10퍼센트() {
            // 7월 금~일 (주말 포함) - 주말이 우선
            LocalDate friday = LocalDate.of(2026, 7, 3);
            LocalDate sunday = LocalDate.of(2026, 7, 5);

            double rate = pointCalculator.determinePointRate(friday, sunday);

            assertThat(rate).isEqualTo(0.10);
        }
    }

    @Nested
    class PointCalculation {

        @Test
        void 비성수기_평일_100000원_5퍼센트_적립() {
            int totalPrice = 100000;
            LocalDate monday = getWeekday(LocalDate.of(2026, 3, 2));

            int points = pointCalculator.calculate(monday, monday, totalPrice);

            assertThat(points).isEqualTo(5000); // 100000 * 0.05
        }

        @Test
        void 주말_포함_100000원_10퍼센트_적립() {
            int totalPrice = 100000;
            LocalDate friday = LocalDate.of(2026, 3, 6);
            LocalDate sunday = LocalDate.of(2026, 3, 8);

            int points = pointCalculator.calculate(friday, sunday, totalPrice);

            assertThat(points).isEqualTo(10000); // 100000 * 0.10
        }

        @Test
        void 성수기_평일_100000원_3퍼센트_적립() {
            int totalPrice = 100000;
            LocalDate monday = getWeekday(LocalDate.of(2026, 7, 6));

            int points = pointCalculator.calculate(monday, monday, totalPrice);

            assertThat(points).isEqualTo(3000); // 100000 * 0.03
        }
    }

    @Nested
    class PointWithAutoPrice {

        @Test
        void 비성수기_평일_대형_1박_포인트() {
            LocalDate monday = getWeekday(LocalDate.of(2026, 3, 2));

            int points = pointCalculator.calculate(monday, monday, "A-1");

            // 가격: 80000원, 적립률: 5%
            assertThat(points).isEqualTo(4000);
        }

        @Test
        void 주말_포함_대형_3박_포인트() {
            LocalDate friday = LocalDate.of(2026, 3, 6);
            LocalDate sunday = LocalDate.of(2026, 3, 8);

            int points = pointCalculator.calculate(friday, sunday, "A-1");

            // 가격: 80000 + 104000 + 104000 = 288000원
            // 적립률: 10% (주말 포함)
            assertThat(points).isEqualTo(28800);
        }
    }

    @Nested
    class WeekendCheck {

        @Test
        void 월화수_주말_미포함() {
            LocalDate monday = LocalDate.of(2026, 3, 2);
            LocalDate wednesday = LocalDate.of(2026, 3, 4);

            assertThat(pointCalculator.hasWeekendInPeriod(monday, wednesday)).isFalse();
        }

        @Test
        void 금토일_주말_포함() {
            LocalDate friday = LocalDate.of(2026, 3, 6);
            LocalDate sunday = LocalDate.of(2026, 3, 8);

            assertThat(pointCalculator.hasWeekendInPeriod(friday, sunday)).isTrue();
        }

        @Test
        void 토요일만_주말_포함() {
            LocalDate saturday = LocalDate.of(2026, 3, 7);

            assertThat(pointCalculator.hasWeekendInPeriod(saturday, saturday)).isTrue();
        }
    }

    @Nested
    class BoundaryTests {

        @ParameterizedTest(name = "{0}월은 성수기 적립률={1}")
        @CsvSource({
                "6, 0.05",   // 비성수기 기본 5%
                "7, 0.03",   // 성수기 3%
                "8, 0.03",   // 성수기 3%
                "9, 0.05"    // 비성수기 기본 5%
        })
        void 성수기_경계_적립률(int month, double expectedRate) {
            LocalDate monday = getWeekday(LocalDate.of(2026, month, 1));
            LocalDate tuesday = monday.plusDays(1);

            double rate = pointCalculator.determinePointRate(monday, tuesday);

            assertThat(rate).isEqualTo(expectedRate);
        }
    }

    // Helper: 평일 찾기
    private LocalDate getWeekday(LocalDate date) {
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.plusDays(1);
        }
        return date;
    }
}
