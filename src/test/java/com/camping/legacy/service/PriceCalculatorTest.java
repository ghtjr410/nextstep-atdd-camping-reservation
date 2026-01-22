package com.camping.legacy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PriceCalculator 단위 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PriceCalculatorTest {

    private PriceCalculator priceCalculator;

    @BeforeEach
    void setUp() {
        priceCalculator = new PriceCalculator();
    }

    @Nested
    @DisplayName("기본 가격")
    class BasePrice {

        @ParameterizedTest(name = "사이트 {0}는 기본가 {1}원")
        @CsvSource({
                "A-1, 80000",
                "A-99, 80000",
                "B-1, 50000",
                "B-99, 50000",
                "C-1, 60000",
                "D-1, 60000"
        })
        void 사이트_종류별_기본_가격(String siteNumber, int expectedPrice) {
            assertThat(priceCalculator.getBasePrice(siteNumber)).isEqualTo(expectedPrice);
        }
    }

    @Nested
    @DisplayName("1박 가격 계산")
    class SingleDayPrice {

        @Test
        void 평일_비성수기_대형_80000원() {
            LocalDate weekday = getWeekday(LocalDate.of(2026, 3, 1)); // 비성수기 평일
            int price = priceCalculator.calculate(weekday, weekday, "A-1");

            assertThat(price).isEqualTo(80000);
        }

        @Test
        void 평일_비성수기_소형_50000원() {
            LocalDate weekday = getWeekday(LocalDate.of(2026, 3, 1));
            int price = priceCalculator.calculate(weekday, weekday, "B-1");

            assertThat(price).isEqualTo(50000);
        }

        @Test
        void 주말_비성수기_30퍼센트_할증() {
            LocalDate saturday = LocalDate.of(2026, 3, 7); // 토요일
            int price = priceCalculator.calculate(saturday, saturday, "A-1");

            assertThat(price).isEqualTo((int)(80000 * 1.3));
        }

        @Test
        void 평일_성수기_50퍼센트_할증() {
            LocalDate julyWeekday = getWeekday(LocalDate.of(2026, 7, 1));
            int price = priceCalculator.calculate(julyWeekday, julyWeekday, "A-1");

            assertThat(price).isEqualTo((int)(80000 * 1.5));
        }

        @Test
        void 주말_성수기_70퍼센트_할증() {
            LocalDate julySaturday = LocalDate.of(2026, 7, 4); // 7월 토요일
            int price = priceCalculator.calculate(julySaturday, julySaturday, "A-1");

            assertThat(price).isEqualTo((int)(80000 * 1.7));
        }
    }

    @Nested
    @DisplayName("복합 기간 계산")
    class MultiDayPrice {

        @Test
        void 평일_2박_합산() {
            LocalDate monday = getWeekday(LocalDate.of(2026, 3, 2));
            LocalDate tuesday = monday.plusDays(1);
            int price = priceCalculator.calculate(monday, tuesday, "A-1");

            assertThat(price).isEqualTo(80000 * 2);
        }

        @Test
        void 금토일_3박_주말할증_적용() {
            LocalDate friday = LocalDate.of(2026, 3, 6);   // 금요일
            LocalDate sunday = LocalDate.of(2026, 3, 8);   // 일요일
            int price = priceCalculator.calculate(friday, sunday, "A-1");

            int expected = 80000                    // 금 (평일)
                    + (int)(80000 * 1.3)           // 토 (주말)
                    + (int)(80000 * 1.3);          // 일 (주말)
            assertThat(price).isEqualTo(expected);
        }

        @Test
        void 성수기_평일_주말_혼합() {
            LocalDate julyFriday = LocalDate.of(2026, 7, 3);   // 7월 금요일
            LocalDate julySunday = LocalDate.of(2026, 7, 5);   // 7월 일요일
            int price = priceCalculator.calculate(julyFriday, julySunday, "A-1");

            int expected = (int)(80000 * 1.5)      // 금 (성수기 평일)
                    + (int)(80000 * 1.7)           // 토 (성수기 주말)
                    + (int)(80000 * 1.7);          // 일 (성수기 주말)
            assertThat(price).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("성수기 경계값")
    class PeakSeasonBoundary {

        @ParameterizedTest(name = "{0}월 1일은 성수기={1}")
        @CsvSource({
                "6, false",
                "7, true",
                "8, true",
                "9, false"
        })
        void 성수기_월_경계(int month, boolean isPeakSeason) {
            LocalDate weekday = getWeekday(LocalDate.of(2026, month, 1));
            int price = priceCalculator.calculate(weekday, weekday, "A-1");

            if (isPeakSeason) {
                assertThat(price).isEqualTo((int)(80000 * 1.5));
            } else {
                assertThat(price).isEqualTo(80000);
            }
        }
    }

    @Nested
    @DisplayName("일별 가격 계산")
    class DailyPrice {

        @Test
        void 할증_적용_메서드_직접_테스트() {
            int basePrice = 80000;

            // 비성수기 평일
            LocalDate weekday = getWeekday(LocalDate.of(2026, 3, 1));
            assertThat(priceCalculator.applyPriceSurcharge(basePrice, weekday))
                    .isEqualTo(80000);

            // 비성수기 주말
            LocalDate saturday = LocalDate.of(2026, 3, 7);
            assertThat(priceCalculator.applyPriceSurcharge(basePrice, saturday))
                    .isEqualTo((int)(80000 * 1.3));

            // 성수기 평일
            LocalDate julyWeekday = getWeekday(LocalDate.of(2026, 7, 1));
            assertThat(priceCalculator.applyPriceSurcharge(basePrice, julyWeekday))
                    .isEqualTo((int)(80000 * 1.5));

            // 성수기 주말
            LocalDate julySaturday = LocalDate.of(2026, 7, 4);
            assertThat(priceCalculator.applyPriceSurcharge(basePrice, julySaturday))
                    .isEqualTo((int)(80000 * 1.7));
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
