# language: ko

Feature: 예약 취소
  고객이 예약을 취소할 수 있다

  Background:
    Given 고객 "홍길동"이 "A-1" 사이트를 예약하고 확인코드를 받았다

  Scenario: 올바른 확인코드로 사전 취소
    Given 예약 시작일이 내일 이후이다
    When 발급받은 확인코드로 예약을 취소한다
    Then 예약 상태가 "CANCELLED"로 변경된다

  Scenario: 당일 취소 시 CANCELLED_SAME_DAY 상태
    Given 예약 시작일이 오늘이다
    When 발급받은 확인코드로 예약을 취소한다
    Then 예약 상태가 "CANCELLED_SAME_DAY"로 변경된다

  Scenario: 잘못된 확인코드로 취소하면 실패
    When 잘못된 확인코드로 예약을 취소한다
    Then 취소가 실패한다

  Scenario: 취소된 예약의 사이트는 재예약 가능
    Given 해당 예약이 취소되었다
    When 고객 "김철수"가 같은 기간에 "A-1" 사이트를 예약한다
    Then 예약이 성공한다
