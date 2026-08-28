# CLAUDE.md - NewVintly Project Guide

## Project Overview

빈티지 커뮤니티 플랫폼 REST API 백엔드. 빈티지 매장 등록/조회, 좋아요, 댓글, 회원 관리 기능을 제공한다.

- **전체 구성:** Flutter(앱) + Spring Boot(본 레포, 백엔드). 프론트는 별도 레포에서 Codex로 작업.
- **배경:** 소규모 친구 그룹용으로 시작, 현재는 이직용 포트폴리오 목적으로 발전 중.
- **도메인:** vintly.co.kr
- **아키텍처 방침:** 모놀리식 유지 (프로젝트 규모상 MSA는 오버엔지니어링으로 판단). 단, 도메인 경계는 의식적으로 분리해 설계한다 (아래 Architecture 참고).
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

프로파일: `local`, `dev`, `prd`, `test`

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

JWT 기반 Stateless 인증, RTR(Refresh Token Rotation) 방식:
- Access 토큰: 응답 헤더 `access`
- Refresh 토큰: HttpOnly 쿠키 + Redis 저장, 재발급 시 Access/Refresh를 함께 회전
- `LoginFilter` → `JWTFilter` → `CustomLogoutFilter`
- 비밀번호: BCrypt
- 역할: `ROLE_USER`, `ROLE_ADMIN`
- 회원 상태: `Use` enum (`Y`=사용, `X`=추방, `E`=탈퇴, `K`=대기). 탈퇴 시 `useStatus=E` + `deletedAt` 기록, 닉네임은 조인 없이 조회 가능하도록 `board`/`board_comment`에 작성 당시 닉네임을 역정규화해 보존한다.
- 소셜 로그인 도입 우선순위(국내 사용률 기준): 카카오 > 네이버 > 구글 > 애플 (미착수)

## Database

- 감사 필드: `BaseEntity`의 `createdAt`, `updatedAt` 자동 관리
- QueryDSL: `BooleanExpression` 빌더로 동적 쿼리, `Projections.constructor`로 DTO 매핑
- DDL: dev/prd은 `update`, test는 `create-drop`

### 게시판(BOARD) 도메인 규칙

- 커뮤니티 게시판은 `board`, `board_img`, `board_like`, `board_comment` 테이블로 구성. 자유게시판 등으로 나누지 않고 **단일 게시판**으로 운영한다 (매장 관련 좋아요/댓글은 `vintage`/`vintagelike`/`vintagecomment`로 별도 도메인).
- **Hard Delete만 사용한다.** `board`, `board_comment` 등 게시판 계열 테이블에 `del_status`, `deleted_at` 같은 소프트 삭제용 컬럼을 두지 않는다. (회원 탈퇴 시각을 남기는 `member.deleted_at`은 별개 — 게시글 삭제 상태와 무관하다.)
- `board`/`board_comment`는 `author_nickname`(VARCHAR30)을 역정규화해 보유한다. 탈퇴/닉네임 변경 이후에도 작성 당시 닉네임을 조인 없이 조회하기 위함.
- 다음은 **의도적으로 제거**한 설계다. 되돌리지 말 것:
  - `category` 컬럼
  - `ON DELETE CASCADE`, 물리 FK (`@JoinColumn`에 `foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)`로 명시)
  - 인덱스 — 추후 인덱스 적용 전/후 성능(EXPLAIN) 비교를 포트폴리오 소재로 남기기 위해 의도적으로 비워둔 상태
- `member` 테이블은 `use_status`, `nickname_updated_at` 컬럼을 보유한다.

## Testing

- 단위 테스트: Mockito mock
- 통합 테스트: Testcontainers (MariaDB 10.11, Redis 7.4.2)
- 이메일 테스트: GreenMail (fake SMTP)
- 비동기 테스트: Awaitility

## CI/CD & 인프라

- Docker Compose(`docker-compose.yml`)로 로컬 MariaDB 11.3 구성 가능.
- **DEV 배포:** 집 NAS의 Synology Container Manager(Docker) — Nginx + Spring Boot(`vintly-backend`) + Redis + MariaDB. Jenkins로 CI, Blue/Green 배포. 리버스 프록시는 Nginx, HTTPS는 Certbot.
- **PRD:** DB는 RDS 사용 예정. RDS 연결은 항상 SSL 필수 — JDBC URL에 `useSSL=true&requireSSL=true`.
- 프로필: `local`, `dev`, `prd`, `test` (`application-{profile}.yml`). local/dev/prd yml은 `.gitignore` 처리되어 있고 실제 자격증명은 커밋되지 않는다 — 새 값 채울 때도 유지할 것.
- Redis 연결 호스트가 환경별로 다르다: 배포(NAS Docker) 환경에서는 컨테이너명 `vintly-redis`, 로컬 개발에서는 NAS IP(또는 `localhost`)를 사용한다.

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
### 0. Swagger 인터페이스 + @Valid 규칙
- 컨트롤러가 Swagger 인터페이스를 `implements`할 때, `@Valid` 등 파라미터 제약 조건은 **인터페이스 메서드에도 동일하게** 선언해야 한다.
- 컨트롤러에만 `@Valid`를 붙이면 `HV000151: OverridingMethodMustNotAlterParameterConstraints` 에러 발생.
- 항상 인터페이스와 구현체의 파라미터 어노테이션을 일치시킬 것.

### 1. Never Do
- 실제 동작하지 않는 코드, 불필요한 Mock 데이터를 이요한 구현을 하지 말 것
- null-safety 하지 않게 코드 작성하지 말 것 (Java 의 경우, Optional 을 활용할 것)
- println 코드 남기지 말 것
- 자격증명(DB 비밀번호, AWS 키, 메일 비밀번호, JWT secret 등)을 커밋하지 말 것. `application-{local,dev,prd,test}.yml`은 `.gitignore` 대상이며 실제 값이 필요하면 `.env`/서버 환경변수 등 별도 경로로 관리
- 게시판(`board` 계열) 테이블에 인덱스, 물리 FK, `category` 컬럼, `ON DELETE CASCADE`를 임의로 추가하지 말 것 (의도적으로 제거된 설계, 위 Database 섹션 참고)
- Soft Delete 방식(예: `del_status`, `deleted_at` 컬럼)으로 되돌리지 말 것 — Hard Delete 유지
- 도메인을 별도 서비스/MSA로 쪼개지 말 것 — 모놀리식 + 도메인 분리 설계 유지

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