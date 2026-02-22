# CLAUDE.md - NewVintly Project Guide

## Project Overview

빈티지 커뮤니티 플랫폼 REST API 백엔드. 빈티지 매장 등록/조회, 좋아요, 댓글, 회원 관리 기능을 제공한다.

- **Language:** Java 17
- **Framework:** Spring Boot 3.4.0
- **Build Tool:** Gradle 8.11.1
- **Database:** MariaDB 11 + Redis
- **ORM:** Spring Data JPA + QueryDSL 5.0.0 (Jakarta)

## Architecture

4-Layer Architecture (DDD 기반):

```
interfaces/   → REST 컨트롤러, Request/Response DTO, 필터, 예외 핸들러
application/  → Facade (유스케이스 조합), Scheduler
domain/       → 엔티티, 도메인 서비스, 리포지토리 인터페이스, 이벤트
infra/        → JPA 구현체, QueryDSL, AWS S3, Redis, Security, Swagger 설정
```

패키지 루트: `com.vintly`

## Build & Run

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행 (로컬)
./gradlew bootRun
```

프로파일: `local`, `dev`, `real`, `test`

## Key Dependencies

- **Auth:** JJWT 0.12.3 (HS256, access/refresh 토큰)
- **Storage:** AWS S3 SDK 2.25.20
- **Docs:** springdoc-openapi 2.7.0 (Swagger UI)
- **Email:** Spring Mail + Thymeleaf 템플릿
- **Logging:** Logstash Logback Encoder (ELK)
- **Testing:** JUnit 5, Mockito, Testcontainers (MariaDB + Redis), GreenMail, Awaitility

## Code Conventions

### Naming

- 엔티티: `Member`, `Vintage`, `VintageComment` (접미사 없음)
- 서비스: `*Service` (도메인 서비스), `*Facade` (애플리케이션 조합)
- 리포지토리: domain에 `*Repository` 인터페이스, infra에 `*RepositoryImpl` + `*JpaRepository` + `*QueryDslRepository`
- 컨트롤러: `*Controller`
- DTO: `*Request`, `*Response`, `*Info` (Java record 사용)
- 예외: `*Exception`

### Style

- Lombok 적극 사용 (`@Getter`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Slf4j`)
- DTO는 Java record로 정의
- 생성자 주입 사용 (필드 주입 금지)
- 읽기 메서드: `@Transactional(readOnly = true)`
- 쓰기 메서드: `@Transactional(rollbackFor = Exception.class)`
- DB 컬럼: snake_case (`member_id`, `created_at`)

### API

- Base path: `/api/v1/`
- 응답 래퍼: `ApiResponse<T>(success, code, msg, data)`
- Jakarta Validation 어노테이션으로 요청 검증

## Authentication

JWT 기반 Stateless 인증:
- Access 토큰: 응답 헤더 `access`
- Refresh 토큰: HttpOnly 쿠키 + Redis 저장
- `LoginFilter` → `JWTFilter` → `CustomLogoutFilter`
- 비밀번호: BCrypt
- 역할: `ROLE_USER`, `ROLE_ADMIN`
- 회원 상태: `Use` enum (`Y`=활성, `N`=탈퇴, `X`=정지, `K`=대기)

## Database

- 감사 필드: `BaseEntity`의 `createdAt`, `updatedAt` 자동 관리
- QueryDSL: `BooleanExpression` 빌더로 동적 쿼리, `Projections.constructor`로 DTO 매핑
- DDL: dev/real은 `update`, test는 `create-drop`

## Testing

- 단위 테스트: Mockito mock
- 통합 테스트: Testcontainers (MariaDB 10.11, Redis 7.4.2)
- 이메일 테스트: GreenMail (fake SMTP)
- 비동기 테스트: Awaitility

## CI/CD

GitHub Actions (`deploy.yml`)로 dev 환경 자동 배포.
Docker Compose로 로컬 MariaDB 11.3 구성 가능.

## 개발 규칙
### 진행 Workflow - 증강 코딩
- **대원칙** : 방향성 및 주요 의사 결정은 개발자에게 제안만 할 수 있으며, 최종 승인된 사항을 기반으로 작업을 수행.
- **중간 결과 보고** : AI 가 반복적인 동작을 하거나, 요청하지 않은 기능을 구현, 테스트 삭제를 임의로 진행할 경우 개발자가 개입.
- **설계 주도권 유지** : AI 가 임의판단을 하지 않고, 방향성에 대한 제안 등을 진행할 수 있으나 개발자의 승인을 받은 후 수행.

### 개발 Workflow - TDD (Red > Green > Refactor)
- 모든 테스트는 3A 원칙으로 작성할 것 (Arrange - Act - Assert)
#### 1. Red Phase : 실패하는 테스트 먼저 작성
- 요구사항을 만족하는 기능 테스트 케이스 작성
- 테스트 예시
#### 2. Green Phase : 테스트를 통과하는 코드 작성
- Red Phase 의 테스트가 모두 통과할 수 있는 코드 작성
- 오버엔지니어링 금지
#### 3. Refactor Phase : 불필요한 코드 제거 및 품질 개선
- 불필요한 private 함수 지양, 객체지향적 코드 작성
- unused import 제거
- 성능 최적화
- 모든 테스트 케이스가 통과해야 함

## 주의사항
### 1. Never Do
- 실제 동작하지 않는 코드, 불필요한 Mock 데이터를 이요한 구현을 하지 말 것
- null-safety 하지 않게 코드 작성하지 말 것 (Java 의 경우, Optional 을 활용할 것)
- println 코드 남기지 말 것

### 2. Recommendation
- 실제 API 를 호출해 확인하는 E2E 테스트 코드 작성
- 재사용 가능한 객체 설계
- 성능 최적화에 대한 대안 및 제안
- 개발 완료된 API 의 경우, `.http/**.http` 에 분류해 작성

### 3. Priority
1. 실제 동작하는 해결책만 고려
2. null-safety, thread-safety 고려
3. 테스트 가능한 구조로 설계
4. 기존 코드 패턴 분석 후 일관성 유지