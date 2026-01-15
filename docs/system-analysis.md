# 시스템 분석 결과

## 1. API 목록

| Method | Endpoint | 목적 | 주요 파라미터 | 비고 |
|--------|----------|------|--------------|------|
| POST | `/api/reservations` | 예약 생성 | customerName, startDate, endDate, siteNumber, phoneNumber | 중복 예약 검증 |
| GET | `/api/reservations/{id}` | 예약 조회 | id | - |
| GET | `/api/reservations` | 예약 목록 | date, customerName (optional) | 필터 조회 |
| PUT | `/api/reservations/{id}` | 예약 수정 | id, confirmationCode, body | 확인코드 필수 |
| DELETE | `/api/reservations/{id}` | 예약 취소 | id, confirmationCode | 당일/사전 구분 |
| GET | `/api/reservations/my` | 내 예약 조회 | name, phone | - |
| GET | `/api/reservations/calendar` | 월별 캘린더 | year, month, siteId | - |
| GET | `/api/sites` | 전체 사이트 목록 | - | - |
| GET | `/api/sites/{siteId}` | 사이트 상세 | siteId | - |
| GET | `/api/sites/{siteNumber}/availability` | 단일 날짜 가용성 | siteNumber, date | - |
| GET | `/api/sites/available` | 가용 사이트 목록 | date | - |
| GET | `/api/sites/search` | 기간별 검색 | startDate, endDate, size | 연박용 |

---

## 2. 핵심 비즈니스 규칙

### 예약 규칙
- 예약 기간은 최대 30일
- 과거 날짜 예약 불가 (startDate >= today)
- 종료일이 시작일보다 이전이면 안 됨
- 동일 사이트, 동일 기간 중복 예약 불가
- 취소된 예약(CANCELLED)은 중복 체크에서 제외되어야 함
- 예약 생성 시 6자리 영숫자 확인코드 자동 발급
- 예약 수정/취소 시 확인코드 검증 필수

### 연박 예약
- 시작일부터 종료일까지 모든 날짜에 대해 가용성 확인 필요
- 중간 날짜도 모두 예약 가능해야 함

### 가격 계산
| 사이트 타입 | 기본가 (1박) | 주말 | 성수기 | 성수기+주말 |
|------------|-------------|------|--------|------------|
| A타입 (대형) | 80,000원 | +30% | +50% | +70% |
| B타입 (소형) | 50,000원 | +30% | +50% | +70% |
| 기타 | 60,000원 | +30% | +50% | +70% |

※ 시작일~종료일 모든 날짜에 대해 개별 계산 후 합산

### 포인트 적립
| 조건 | 적립률 |
|------|--------|
| 기본 | 5% |
| 주말 포함 | 10% |
| 성수기 | 3% |

### 결제수단별 포인트
| 결제수단 | 적립률 |
|----------|--------|
| 카드 | 10% |
| 모바일 | 8% |
| 계좌이체 | 5% |
| 현금 | 3% |

### 예약 상태
| 상태 | 설명 | 조건 |
|------|------|------|
| CONFIRMED | 예약 확정 | 기본값 |
| CANCELLED | 사전 취소 | startDate > today |
| CANCELLED_SAME_DAY | 당일 취소 | startDate == today |

### 사이트 정보
- A타입: 대형, 전기 사용 가능
- B타입: 소형, 전기 사용 불가

---

## 3. 주요 문제점 (리팩토링 대상)

### 3.1 동시성 문제
- `Thread.sleep(100)` 의도적 지연으로 Race Condition 발생
- 낙관적/비관적 잠금 없음 → 중복 예약 가능
- 동시 요청 시 두 예약 모두 성공할 수 있음

### 3.2 코드 복잡도
- `createReservation()` 200줄+, 중첩 깊이 5단계
- 단일 책임 위반: 검증, 가격계산, 포인트, 알림까지 한 메서드에서 처리
- `ReservationService`가 7가지 책임 담당 (CRUD, 캘린더, 통계, 가격, 포인트, 알림, 가용성)

### 3.3 코드 중복
- **가격 계산**: 3곳에서 동일 로직 반복
- **날짜 검증**: 5곳에서 중복
- **DTO 변환**: 4곳에서 수동 변환 (ReservationResponse.from() 미활용)

### 3.4 버그 가능성
| 버그 | 설명 | 심각도 |
|------|------|--------|
| 기간 검색 중간 날짜 미체크 | 시작일/종료일만 확인, 중간 날짜 무시 | 높음 |
| 예약 수정 시 중복 체크 없음 | 날짜/사이트 변경해도 충돌 검증 안 함 | 높음 |
| 취소된 예약도 중복 판정 | status 조건 없이 체크 | 높음 |
| 가용성 체크 불일치 | 메서드마다 다른 기준 사용 | 중간 |

**버그 상세:**
```
[기간 검색 버그]
예: 1월 20~22일 검색 시
- 코드: 20일, 22일만 확인
- 문제: 21일에 예약 있어도 "가용"으로 표시

[취소된 예약 버그]
- 명세서: "취소된 예약은 중복 체크에서 제외"
- 코드: status 조건 없이 전체 체크
- 문제: 취소된 사이트에 새 예약 불가
```

### 3.5 테스트 어려움
- 테스트 코드 전무
- `LocalDate.now()` 직접 호출 → 시간 고정 불가
- 모든 예외가 `RuntimeException` → 예외 타입 구분 불가
- 쿼리 비효율: `findAll()` 후 메모리 필터링

### 3.6 유지보수 문제
- Deprecated 코드 방치 (CalendarService, processReservationWithPayment)
- 상수 중복 정의 (MAX_RESERVATION_DAYS가 여러 곳에)
- 하드코딩된 가격/할증율

---

## 4. 명세서-코드 불일치

| 명세서 | 코드 현황 |
|--------|----------|
| 취소된 예약은 중복 체크 제외 | 미구현 (status 조건 없음) |
| 연박 시 전체 기간 가용성 확인 | 시작일/종료일만 확인 |
| 동시 요청 시 하나만 성공 | Thread.sleep으로 오히려 문제 유발 |
| 오늘로부터 30일 이내 예약 | 예약 기간이 30일 이내인지만 체크 |
