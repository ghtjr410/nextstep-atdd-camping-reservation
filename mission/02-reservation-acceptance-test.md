# 2단계 - 인수 테스트 구현

## 코드 리뷰
> PR 링크:
> **[https://github.com/next-step/atdd-camping-reservation/pull/82](https://github.com/next-step/atdd-camping-reservation/pull/82)**


## 요구사항

### 1. RestAssured 테스트 환경 구축
- RestAssured 의존성 추가
- 테스트 환경 설정 (baseURI, port, logging)
- 테스트 데이터 초기화 전략 수립

### 2. 인수 테스트 구현
- 1단계에서 도출한 인수 조건에 대한 테스트 작성
- 정상 케이스와 예외 케이스 모두 구현

### 3. 테스트 데이터 관리
- 테스트별 독립성 보장
- 데이터 생성/정리 메서드 구현
- 테스트 순서 의존성 제거

## 제출 산출물
### 1. XXXAcceptanceTest.java
```java
@SpringBootTest
class XXXAcceptanceTest {
    
    @BeforeEach
    void setUp() {
        // 테스트 초기화
    }

    @Test
    void [정상_케이스_테스트명]() {
        // Given
        // When
        // Then
    }

    @Test
    void [예외_케이스_테스트명]() {
        // Given
        // When
        // Then
    }
}
```

### 2. 그 외 테스트 클래스
- 테스트 사전값 설정 로직
- 테스트 초기화 로직
- ...

## 체크포인트
- [ ] 테스트가 독립적으로 실행되는가?
- [ ] 테스트 실패 시 원인을 명확히 알 수 있는가?
- [ ] 비즈니스 요구사항이 테스트에 반영되었는가?
- [ ] 테스트 코드가 프로덕션 코드만큼 깔끔한가?
- [ ] CI에서도 실행 가능한가?
   
## 💡 힌트

### 테스트 데이터 격리
- @DirtiesContext 사용 고려 가능
- @Sql을 활용한 쿼리 수행 고려 가능
- 비즈니스 로직(서비스 클래스 활용) 활용 고려 가능

### 테스트 가독성
- 테스트 메서드명은 한글로 작성 가능
- 비즈니스 용어 사용
- 매직 넘버 대신 의미 있는 상수 사용
- 복잡한 assertion은 custom matcher로 추출