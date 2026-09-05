# 신고 기능

게시글·게시판 댓글·매장 댓글에 대한 신고를 접수한다. 관리자 검토와 제재는 아직 없으며 이 문서 5장에 계획으로만 남긴다.

---

## 1. 배포 전 반드시 할 것

**`db/migration/2026-09-03_report_table.sql`을 먼저 실행한다.**

그냥 배포하면 `ddl-auto: update`가 엔티티에서 테이블을 만들면서 enum 값 목록을 `CHECK` 제약으로 붙인다.

```sql
`reason` varchar(20) NOT NULL CHECK (`reason` in ('OBSCENE','ABUSE','SPAM','FLOOD','ETC'))
```

그러면 신고 사유를 하나 추가할 때마다 환경별로 제약을 손봐야 하고, `ddl-auto: update`는 기존 컬럼·제약을 바꾸지 않으므로 운영에서 조용히 저장이 실패한다. 마이그레이션으로 테이블을 먼저 만들어 두면 Hibernate는 기존 테이블을 그대로 둔다.

적용 현황은 마이그레이션 파일 상단에 기록한다.

---

## 2. 결정사항

| # | 결정 | 선택 | 근거 |
| :-- | :--- | :--- | :--- |
| 1 | 1차 구현 범위 | **접수까지** | 접수가 되면 신고는 쌓인다. 처리는 관리자가 DB를 직접 보고 기존 삭제 경로로 대응할 수 있어 관리자 화면 없이도 기능이 성립한다. |
| 2 | 신고 테이블 구조 | **`target_type` + `target_id` 단일 테이블** | 3장 참고. |
| 3 | 블라인드(임시 숨김) | **도입 안 함** | `board` 계열에 상태 컬럼을 두지 않는 설계 원칙과 충돌. 필요해지면 `content_block` 별도 테이블로 추가. |
| 4 | 중복 신고 차단 | **복합 UNIQUE + 애플리케이션 선체크 병행** | 3장 참고. |
| 5 | 신고자 탈퇴 시 | **신고 이력 보존 (`reporter_id` orphan)** | 감사 로그 성격이라 지우면 제재 근거가 사라진다. `reporter_id`는 숫자라 개인정보도 아니다. `CLAUDE.md` 탈퇴 절차에도 명시했다. |
| 6 | enum 저장 방식 | **varchar** (`@JdbcTypeCode(SqlTypes.VARCHAR)`) | 관리자가 DB를 직접 조회하므로 값이 그대로 보여야 한다. 3장의 Hibernate 함정 참고. |
| 7 | 제재 수단 (후속) | 경고 + 기간정지 + 영구추방 | 일반적인 3단계. 강도 조절이 가능하다. |
| 8 | 관리자 계정 (후속) | DB에서 직접 `role` 변경 | 관리자가 극소수. 승격 API를 만들지 않으면 권한 상승 경로 자체가 생기지 않는다. |

---

## 3. 데이터 모델

```
report
  report_id       PK
  reporter_id                           -- 신고자 (탈퇴 시 orphan)
  target_type     VARCHAR  BOARD | BOARD_COMMENT | VINTAGE_COMMENT
  target_id                             -- 대상 PK (대상 삭제 시 orphan)
  reason          VARCHAR  OBSCENE | ABUSE | SPAM | FLOOD | ETC
  detail          TEXT                  -- 상세 사유 (선택)
  status          VARCHAR  PENDING | ACCEPTED | REJECTED
  created_at, updated_at
  UNIQUE (reporter_id, target_type, target_id)
```

`handled_by` / `handled_at`은 검토 기능이 없는 동안 항상 null이라 만들지 않았다. 둘 다 nullable이므로 후속 단계에서 `ddl-auto: update`가 추가한다.

### 왜 대상별로 테이블을 쪼개지 않는가

폴리모픽 참조의 통상적인 대가는 *DB가 참조 무결성을 보장하지 못한다*는 것인데, 이 프로젝트는 이미 `board` 계열에 물리 FK와 `ON DELETE CASCADE`를 두지 않기로 했다. **잃을 무결성이 애초에 없으므로 이 비용은 0이다.** 대신 얻는 것:

- 관리자 신고 목록이 한 번의 쿼리로 끝난다. 테이블을 쪼개면 종류별 조회 후 애플리케이션 병합이나 `UNION ALL` + 페이징이 필요한데, 정렬·전체 건수 계산이 지저분해진다.
- 신고 대상은 늘어나기 쉽다(향후 프로필·매장 자체). 테이블 분리면 대상 하나마다 테이블·엔티티·리포지토리가 한 벌씩 늘지만, 단일 테이블은 enum 상수 하나가 는다.

치르는 값은 **대상 원문을 읽을 때 `target_type` 분기가 필요하다**는 것 하나다. 테이블을 쪼개도 동일하게 필요하다.

### UNIQUE 제약

세 컬럼 각각이 아니라 **조합**이 유일하다.

| reporter_id | target_type | target_id | 결과 |
| :-- | :--- | :-- | :--- |
| 7 | BOARD | 100 | 통과 |
| 7 | BOARD_COMMENT | 100 | 통과 — `target_id`는 같아도 타입이 다름 |
| 8 | BOARD | 100 | 통과 — 다른 사람 |
| 7 | BOARD | 100 | **거부** — 완전히 동일 |

`target_type`이 반드시 포함돼야 한다. 빼면 `board` 100번을 신고한 사람이 `board_comment` 100번을 신고하지 못한다.

**선체크와 제약을 함께 둔다.** 정상 흐름은 `exists` 선체크가 걸러 명확한 예외를 주고, 제약은 동시 요청이 양쪽 다 선체크를 통과하는 창을 막는다. 그래서 리포지토리의 `save`는 `saveAndFlush`다 — 지연 플러시로 두면 제약 위반이 커밋 시점에 터져 409로 변환하지 못하고 500이 나간다.

인덱스 금지 방침의 예외인 이유는 조회 성능이 아니라 정합성이 목적이기 때문이다. 조회용 인덱스와 물리 FK는 게시판 계열과 동일하게 두지 않는다.

### Hibernate enum 함정

**`@Enumerated(STRING)`만으로는 varchar가 되지 않는다.** Hibernate 6은 MariaDB에서 이를 네이티브 `ENUM` 타입으로 만들고 `length`도 무시한다. `@JdbcTypeCode(SqlTypes.VARCHAR)`를 함께 붙여야 한다. `ReportRepositoryIntegrationTest`가 `information_schema`로 컬럼 타입을 확인해 되돌아가는 것을 막는다.

varchar로 바꿔도 `CHECK` 제약이 붙는 문제가 남는다 — 1장 참고.

### 작성자 판정의 null

`vintage_comment`는 작성자 탈퇴 시 `member_id`가 null이 된다(`board`/`board_comment`는 NOT NULL이라 orphan 값이 남는다). 그래서 `ReportFacade`의 작성자 조회는 `Optional<Long>`을 반환하되 **빈 값이 "대상 없음"이 아니라 "작성자 없음"** 을 뜻한다. 대상이 없는 경우는 그전에 예외로 빠진다. 이걸 뒤섞으면 탈퇴자가 남긴 댓글 신고가 404로 나간다.

---

## 4. 구현된 것

```
POST /api/v1/reports          접수 (본인 콘텐츠 403, 중복 409, 없는 대상 404)
GET  /api/v1/reports/me       내 신고 내역 (최신순)
```

| 계층 | 위치 |
| :--- | :--- |
| 엔티티 · enum | `domain/report/` |
| 리포지토리 | `domain/report/repo/` + `infra/report/` |
| 서비스 | `domain/report/service/ReportService` |
| 조합 | `application/report/ReportFacade` — 대상 존재 확인, 자기 콘텐츠 판정 |
| API | `interfaces/report/` + `infra/config/swagger/api/SwaggerReportApi` |
| 호출 예시 | `.http/report.http` |

대상 존재 확인은 `ReportTargetType` 분기(`switch`)로 둔다. 대상이 3종뿐이라 지금은 이걸로 충분하고, 늘어나면 enum에 확인 전략을 붙이는 형태가 자연스럽다.

오류는 HTTP 상태가 아니라 응답 본문의 `code`로 나간다(기존 API 전체 규약). 실패해도 HTTP는 200이다.

---

## 5. 후속 계획

접수가 쌓이기 시작한 뒤 착수한다.

### 1단계 · 관리자 기반 (보안, 선행 필수)

```java
// SecurityConfig — 현재 상태
.requestMatchers("/admin").hasRole("ADMIN")   // 정확히 "/admin" 한 경로만 매칭
.anyRequest().authenticated()
```

이 상태로 `/api/v1/admin/**`에 관리자 API를 만들면 **로그인한 아무 회원이나 호출할 수 있다.** 지금은 관리자 엔드포인트가 없어 노출된 것은 없다.

- `SecurityConfig`에 `/api/v1/admin/**` → `hasRole("ADMIN")` 추가
- `interfaces/admin/AdminController` 신설
- 방치된 `BoardFacade.deleteBoardByAdmin` 연결 (구현돼 있으나 호출처 0건)
- 관리자 계정: `UPDATE member SET role = 'ROLE_ADMIN' WHERE member_id = ?`

**검증** — 일반 회원 토큰으로 관리자 API 호출 시 403이 나오는지 통합 테스트로 확인.

### 2단계 · 검토 · 처리

```
GET   /api/v1/admin/reports          상태·유형별 목록 (QueryDSL 동적 조건 + 페이징)
GET   /api/v1/admin/reports/{id}     신고 내용 + 대상 콘텐츠 원문
PATCH /api/v1/admin/reports/{id}     ACCEPTED(삭제) / REJECTED(기각)
```

- `report`에 `handled_by`, `handled_at` 추가
- 상세 조회에서 대상 콘텐츠 원문을 함께 반환한다. 삭제 여부를 판단하려면 원문이 필요하다.
- `ACCEPTED` 처리 순서: ① 대상 삭제(게시글이면 댓글·이미지까지) ② 같은 대상의 다른 `PENDING` 신고 일괄 완료 ③ 처리자·처리시각 기록

**함께 정리할 기존 문제** — `deleteBoard`, `deleteBoardByAdmin` 어느 쪽도 `board_img` 행이나 S3 객체를 정리하지 않아 양쪽에 orphan이 쌓인다. 신고 삭제가 이 경로를 자주 타므로 이 단계에서 함께 처리한다.

### 3단계 · 제재

```
POST /api/v1/admin/members/{id}/sanctions      경고 / 정지 / 추방
GET  /api/v1/admin/members/{id}/sanctions      누적 이력 (반복 위반 판단용)
```

```
member_sanction
  sanction_id     PK
  member_id                             -- 제재 대상
  type            WARN | SUSPEND | BAN
  reason
  report_id                             -- 근거 신고 (nullable)
  expires_at                            -- SUSPEND일 때만
  created_by                            -- 제재한 관리자
  created_at

member  + suspended_until   DATETIME NULL
```

| 유형 | 동작 |
| :--- | :--- |
| `WARN` | 이력만 기록 |
| `SUSPEND` | `Use.S` 추가 + `suspended_until` 설정 |
| `BAN` | 기존 `Member.vanMember()` 재사용 → `Use.X` |

`CustomUserDetails.isAccountNonLocked`는 이미 `Use.X`면 로그인을 막는다. 추방 제재의 절반은 동작하는 셈이다.

**정지 만료 해제는 로그인 시점 lazy 처리**를 택한다. 스케줄러보다 단순하고, 만료됐는데 아직 스케줄러가 돌지 않아 로그인이 막히는 창이 생기지 않는다.

```java
if (useStatus == Use.X) return false;                         // 영구 추방
if (useStatus == Use.S) return suspendedUntil.isBefore(now);  // 기간 만료 시 통과
return true;
```

**제재 즉시 Redis의 `refresh:{email}`을 폐기해야 한다.** 폐기하지 않으면 정지된 사용자가 refresh 토큰을 계속 회전시킬 수 있다. 탈퇴 작업에서 만든 `AuthService.deleteRefreshToken()`을 그대로 쓴다.

> 이미 발급된 access 토큰은 만료(최대 30분)까지 유효하다. `JWTFilter`가 DB를 조회하지 않기 때문으로, 회원 탈퇴에서 수용하기로 한 것과 동일한 성질의 문제다.
