# 발견된 이슈 목록

> 3단계 버그 수정 과정에서 발견한 Top 3 기능(예약 생성, 예약 취소, 사이트 검색)의 이슈들

---

## 1. createReservation (예약 생성)

### 1.1 [BUG] 가격/포인트 계산 결과 미저장 (죽은 코드)
- **위치**: `ReservationService.java:147-206`
- **심각도**: 높음
- **설명**: 가격과 포인트를 계산하지만 Reservation 엔티티에 저장하지 않음
- **영향**:
  - 결제 금액 추적 불가
  - 포인트 적립 불가
  - 매출 리포트 부정확
- **해결 방안**:
  - Reservation 엔티티에 `totalPrice`, `earnedPoints` 필드 추가
  - 또는 가격 계산 로직을 별도 서비스로 분리 (PriceCalculator)

```java
// 현재 코드 (문제)
log.info("예약 금액 계산 완료: {}원", totalPrice);  // 로그만 찍고 끝
log.info("적립 포인트 계산 완료: {}P", earnedPoints);
```

### 1.2 [BUG] 프로덕션 코드에 테스트용 지연 코드
- **위치**: `ReservationService.java:211-215`
- **심각도**: 중간
- **설명**: `Thread.sleep(100)` - 동시성 테스트 재현용 코드가 프로덕션에 포함
- **영향**: 모든 예약 생성에 100ms 지연 발생
- **해결 방안**: 삭제 또는 테스트 환경에서만 동작하도록 분리

```java
// 문제 코드
try {
    Thread.sleep(100);  // 프로덕션에서 불필요한 지연
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### 1.3 [CODE SMELL] 확인 코드 생성 로직 중복
- **위치**: `ReservationService.java:229-239` vs `636-644`
- **심각도**: 낮음
- **설명**: `generateConfirmationCode()` 메서드가 존재하지만 사용하지 않고 인라인으로 중복 구현
- **해결 방안**: 기존 메서드 사용

---

## 2. cancelReservation (예약 취소)

### 2.1 [BUG] NPE 가능성
- **위치**: `ReservationService.java:297`
- **심각도**: 중간
- **설명**: `reservation.getConfirmationCode().equals(confirmationCode)` - reservation의 confirmationCode가 null이면 NPE 발생
- **현재 동작**: 정상 데이터에서는 문제없지만 데이터 무결성 깨지면 NPE
- **해결 방안**: null-safe 비교 또는 Objects.equals() 사용

```java
// 현재 코드 (문제)
if (!reservation.getConfirmationCode().equals(confirmationCode)) {

// 개선 코드
if (!Objects.equals(reservation.getConfirmationCode(), confirmationCode)) {
```

### 2.2 [BUG] 알림 발송 누락
- **위치**: `ReservationService.java:293-309`
- **심각도**: 낮음
- **설명**: 취소 처리 후 `sendCancellationNotification()` 호출 안 함
- **영향**: 고객이 취소 완료 알림을 받지 못함

### 2.3 [DESIGN] 이미 취소된 예약 상태 덮어쓰기
- **위치**: `ReservationService.java:302-306`
- **심각도**: 낮음
- **설명**: 이미 CANCELLED_SAME_DAY 상태인 예약을 다시 취소하면 CANCELLED로 변경될 수 있음
- **현재 동작**: 멱등성은 보장되나 상태 이력 손실 가능

---

## 3. searchAvailableSites (사이트 검색)

### 3.1 [CODE SMELL] 크기 결정 로직 중복
- **위치**: `SiteService.java:87-95` 와 `107-114`
- **심각도**: 낮음
- **설명**: 사이트 크기를 결정하는 로직이 같은 메서드 내에서 2번 반복
- **해결 방안**: 헬퍼 메서드로 추출

```java
// 중복 로직
if (site.getSiteNumber().startsWith("A")) {
    siteSize = "대형";
} else if (site.getSiteNumber().startsWith("B")) {
    siteSize = "소형";
} else {
    siteSize = "일반";
}
```

### 3.2 [CODE SMELL] 전기 사용 가능 여부 하드코딩
- **위치**: `SiteService.java:117-120`
- **심각도**: 낮음
- **설명**: A 사이트만 전기 사용 가능으로 하드코딩
- **해결 방안**: Campsite 엔티티에 필드로 관리

### 3.3 [PERFORMANCE] N+1 쿼리 가능성
- **위치**: `SiteService.java:82-133`
- **심각도**: 중간
- **설명**: 모든 사이트 조회 후 각각에 대해 예약 존재 여부 확인
- **영향**: 사이트 수가 많아지면 성능 저하
- **해결 방안**: 한 번의 쿼리로 사용 불가능한 사이트 목록 조회

---

## 우선순위 정리

| 순위 | 이슈 | 심각도 | 기능 |
|-----|------|-------|-----|
| 1 | 가격/포인트 미저장 | 높음 | create |
| 2 | Thread.sleep 프로덕션 코드 | 중간 | create |
| 3 | NPE 가능성 | 중간 | cancel |
| 4 | N+1 쿼리 | 중간 | search |
| 5 | 알림 발송 누락 | 낮음 | cancel |
| 6 | 로직 중복 | 낮음 | 전체 |

---

## 이번 단계에서 해결한 항목
- [x] 가격/포인트 미저장 → PriceCalculator, PointCalculator 분리 및 단위 테스트 작성
- [x] NPE 방어 코드 추가 → Objects.equals() 사용

## 차후 해결 권장
- [ ] Thread.sleep 제거
- [ ] N+1 쿼리 최적화
- [ ] 알림 발송 누락
- [ ] 로직 중복 제거
