<div align="center">

# Vintly

**빈티지 커뮤니티 플랫폼 REST API**

빈티지 매장을 등록하고 공유하며, 좋아요와 댓글로 소통하는 커뮤니티 백엔드입니다.

<br/>

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.11.1-02303A?style=flat-square&logo=gradle&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-11-003545?style=flat-square&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)

</div>

---

## 목차

- [프로젝트 구성](#프로젝트-구성)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [시작하기](#시작하기)
- [API 문서](#api-문서)
- [인증](#인증)
- [테스트](#테스트)
- [배포](#배포)

---

## 프로젝트 구성

| 구분 | 내용 |
| :--- | :--- |
| 서비스 | [vintly.co.kr](https://vintly.co.kr) |
| 백엔드 | 본 저장소 — Spring Boot REST API |
| 앱 | Flutter (별도 저장소) |
| 아키텍처 방침 | 모놀리식 유지, 단 도메인 경계는 의식적으로 분리 |

주요 도메인은 `member`(회원), `vintage`(빈티지 매장), `board`(커뮤니티 게시판), `auth`(인증)이며, 매장·게시판 각각에 좋아요와 댓글이 붙습니다.

---

## 기술 스택

<table>
<tr><td><b>Language</b></td><td>Java 17</td></tr>
<tr><td><b>Framework</b></td><td>Spring Boot 3.4.0</td></tr>
<tr><td><b>Build</b></td><td>Gradle 8.11.1</td></tr>
<tr><td><b>Database</b></td><td>MariaDB 11, Redis</td></tr>
<tr><td><b>ORM</b></td><td>Spring Data JPA, QueryDSL 5.0.0 (Jakarta)</td></tr>
<tr><td><b>Auth</b></td><td>Spring Security, JJWT 0.12.3 (HS256)</td></tr>
<tr><td><b>Storage</b></td><td>AWS S3 SDK 2.25.20</td></tr>
<tr><td><b>Docs</b></td><td>springdoc-openapi 2.7.0</td></tr>
<tr><td><b>Mail</b></td><td>Spring Mail, Thymeleaf</td></tr>
<tr><td><b>Logging</b></td><td>Logstash Logback Encoder (ELK)</td></tr>
<tr><td><b>Test</b></td><td>JUnit 5, Mockito, Testcontainers, GreenMail, Awaitility</td></tr>
</table>

---

## 아키텍처

DDD 기반 4계층 구조입니다. 패키지 루트는 `com.vintly`.

```
interfaces/    REST 컨트롤러, Request/Response DTO, 필터, 예외 핸들러
     ↓
application/   Facade (유스케이스 조합), Scheduler
     ↓
domain/        엔티티, 도메인 서비스, 리포지토리 인터페이스, 이벤트
     ↓
infra/         JPA 구현체, QueryDSL, AWS S3, Redis, Security, Swagger 설정
```

리포지토리는 `domain`에 인터페이스를 두고 `infra`에서 구현해, 도메인이 영속성 기술에 의존하지 않도록 분리했습니다.

```
domain/board/repo/BoardRepository.java          ← 인터페이스
infra/board/BoardRepositoryImpl.java            ← 구현
infra/board/BoardJpaRepository.java             ← Spring Data JPA
infra/board/BoardQueryDslRepository.java        ← 동적 쿼리
```

---

## 시작하기

**요구사항** — JDK 17, Docker

```bash
# 로컬 DB 실행 (선택)
docker compose up -d

# 빌드
./gradlew build

# 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

프로파일은 `local`, `dev`, `prd`, `test` 네 가지입니다.
`application-{local,dev,prd}.yml`은 자격증명을 담고 있어 저장소에 포함되지 않으므로, 직접 준비해야 합니다.

---

## API 문서

애플리케이션 실행 후 Swagger UI에서 확인합니다.

```
http://localhost:8080/swagger-ui/index.html
```

모든 엔드포인트는 `/api/v1/`을 기준 경로로 하며, 응답은 아래 형태로 감싸집니다.

```json
{
  "success": true,
  "code": 200,
  "msg": "",
  "data": { }
}
```

---

## 인증

JWT 기반 Stateless 인증에 **RTR(Refresh Token Rotation)** 을 적용했습니다.

| 토큰 | 전달 방식 | 저장 | 만료 |
| :--- | :--- | :--- | :--- |
| Access | 응답 헤더 `access` | — | 30분 |
| Refresh | HttpOnly 쿠키 | Redis | 3일 |

재발급 시 Access와 Refresh를 함께 회전시키고, 이전 Refresh 토큰은 폐기합니다.
요청은 `LoginFilter` → `JWTFilter` → `CustomLogoutFilter` 순으로 처리되며, 비밀번호는 BCrypt로 암호화합니다.

회원 탈퇴는 **하드 삭제**입니다. `member` 행을 실제로 지우고, 작성한 글과 댓글은 작성자명을 익명화한 채 남기며, 좋아요와 Redis의 Refresh 토큰은 함께 제거합니다.

---

## 테스트

```bash
./gradlew test
```

- **단위 테스트** — Mockito 기반. Spring 컨텍스트 없이 동작합니다.
- **통합 테스트** — Testcontainers로 MariaDB·Redis를 띄워 실제 쿼리 동작까지 검증합니다.

> **Docker가 실행 중이어야 합니다.** 꺼져 있으면 통합 테스트만 실패하며, 코드 문제가 아니라 컨테이너를 띄우지 못해서입니다. 첫 실행 시 이미지를 내려받느라 다소 느릴 수 있습니다.

스키마 변경이 필요한 DDL은 `db/migration/`에 날짜별 `.sql`로 남기고, 환경별 적용 여부를 파일 상단에 기록합니다.

---

## 배포

Docker 기반으로 운영합니다.

```
Nginx (리버스 프록시, HTTPS)
  └─ Spring Boot (vintly-backend)
       ├─ MariaDB
       └─ Redis
```

Jenkins로 CI를 구성했고 Blue/Green 방식으로 무중단 배포하며, HTTPS 인증서는 Certbot으로 관리합니다.
