# N+1 문제 — 차단 목록 조회로 보는 실제 사례

> 학습용 메모. 2026-09-03 차단 기능 작업 중 정리.
> 이 프로젝트의 실제 코드와 실측값을 기준으로 한다.

---

## 1. 실측 결과부터

차단 10건을 만들어 놓고 목록을 조회하면서 Hibernate가 실제로 날린 쿼리 수를 셌다.

| 방식 | 쿼리 수 |
| :--- | :--- |
| `JOIN FETCH` 사용 (현재 구현) | **1회** |
| 지연 로딩 그대로 | **11회** |

11 = 차단 목록 1번 + 각 회원 닉네임 10번. 이게 **N+1**이다. N개를 가져오는 쿼리 1번에, 각각을 채우는 쿼리 N번이 따라붙는다.

차단이 100건이면 101번이 된다. 데이터가 늘수록 쿼리 수가 **선형으로** 늘어난다.

측정 방법은 6장 참고.

---

## 2. 왜 생기는가

`MemberBlock` 엔티티는 상대 회원을 **지연 로딩**으로 잡고 있다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "blocked_id", ...)
private Member blocked;
```

`LAZY`는 "실제로 쓸 때까지 DB에 안 간다"는 뜻이다. 그래서 목록을 조회한 시점에는 `blocked` 자리에 **프록시(가짜 객체)** 만 들어 있다.

```java
List<MemberBlock> blocks = repository.findAllByBlockerId(myId);   // 쿼리 1번
```

여기까지는 쿼리 1번이다. 문제는 다음이다.

```java
blocks.stream().map(MemberBlockInfo.Blocked::from).toList();
```

`Blocked.from()` 안에서 이걸 한다.

```java
block.getBlocked().getNickname()   // ← 프록시가 깨어나면서 SELECT 발생
```

닉네임은 프록시에 없으니 그때 DB를 다녀온다. 반복문이 10번 돌면 **SELECT가 10번** 나간다.

### 핵심

**N+1은 "조회할 때"가 아니라 "쓸 때" 터진다.** 리포지토리 코드만 봐서는 안 보이고, 그 결과를 어떻게 쓰는지를 같이 봐야 한다. 이게 발견이 늦는 이유다.

---

## 3. 이 프로젝트에서 어떻게 막았나

`JOIN FETCH`로 처음부터 함께 가져온다.

```java
// MemberBlockJpaRepository
@Query("SELECT mb FROM member_block mb JOIN FETCH mb.blocked WHERE mb.blocker.memberId = :blockerId ORDER BY mb.blockId DESC")
List<MemberBlock> findAllByBlockerId(@Param("blockerId") Long blockerId);
```

`JOIN`과 `JOIN FETCH`는 다르다.

| | 하는 일 |
| :--- | :--- |
| `JOIN` | 조인해서 **조건에만** 쓴다. 연관 엔티티는 여전히 프록시 |
| `JOIN FETCH` | 조인해서 **함께 가져와 채운다**. 프록시가 아니라 실제 객체 |

`JOIN`만 쓰고 N+1이 그대로인 경우가 흔하다. 조인했으니 됐겠지 싶지만 아니다.

---

## 4. 반대 사례 — 여기선 안 터진다

같은 구조인데 N+1이 없는 코드가 이 프로젝트에 있다. 신고 목록이다.

```java
// ReportJpaRepository — JOIN FETCH 없음
List<Report> findAllByReporterMemberIdOrderByReportIdDesc(Long reporterId);
```

`Report.reporter`도 `LAZY`인데 왜 괜찮은가? **쓰지 않기 때문이다.**

```java
// ReportInfo.My.from
return new My(
        report.getReportId(),
        report.getTargetType(),
        report.getTargetId(),
        report.getReason(),
        report.getDetail(),
        report.getStatus(),
        report.getCreatedAt()
);
```

`getReporter()`를 한 번도 부르지 않는다. 내 신고 내역이라 신고자는 나 자신이고, 응답에 담을 이유가 없다. 프록시는 끝까지 깨어나지 않는다.

**교훈** — `LAZY` 자체가 문제가 아니다. 오히려 `LAZY`가 기본값이어야 한다. 문제는 *많은 행을 조회한 뒤 각 행의 연관을 건드리는 것*이다.

> 참고: `getReporter().getMemberId()`처럼 **식별자만** 꺼내는 것은 프록시를 깨우지 않는다. 프록시가 이미 ID를 알고 있기 때문이다. `ReportFacade`가 작성자 판정에서 이걸 이용한다.
> 단, `@Id` 필드에 직접 접근할 때만 해당한다. `getNickname()`은 당연히 깨운다.

---

## 5. 해결 수단 비교

### (1) `JOIN FETCH` — 이번에 쓴 방법

```java
@Query("SELECT mb FROM member_block mb JOIN FETCH mb.blocked WHERE ...")
```

- 명시적이고 쿼리 1번으로 끝난다.
- **페이징(`Pageable`)과 같이 쓰면 위험하다.** 컬렉션을 fetch join 하면서 페이징하면 Hibernate가 전체를 메모리로 읽고 자바에서 자른다(`HHH90003004` 경고). `@ManyToOne` fetch join은 행 수가 늘지 않아 괜찮지만, `@OneToMany`는 안 된다.
- 컬렉션을 두 개 이상 fetch join 하면 `MultipleBagFetchException`이 난다.

### (2) `@EntityGraph`

```java
@EntityGraph(attributePaths = "blocked")
List<MemberBlock> findAllByBlockerMemberId(Long blockerId);
```

- JPQL을 안 써도 되고 파생 쿼리에 그대로 붙는다.
- 하는 일은 `JOIN FETCH`와 같다(LEFT JOIN으로 나간다).

### (3) DTO 직접 조회 — 이 프로젝트가 목록에서 주로 쓰는 방식

```java
// BoardCommentQueryDslRepository
.select(Projections.constructor(BoardInfo.Comment.class, ...))
```

- 엔티티를 아예 안 만들고 필요한 컬럼만 가져온다. 지연 로딩이 개입할 여지가 없다.
- 가장 빠르고 메모리도 적게 쓴다.
- 대신 조회 전용이라 그 결과로 수정은 못 한다.

### (4) `default_batch_fetch_size`

```yaml
spring.jpa.properties.hibernate.default_batch_fetch_size: 100
```

- 프록시를 깨울 때 하나씩이 아니라 `IN (...)`으로 묶어서 가져온다. **11회 → 2회**가 된다.
- 전역 설정이라 코드를 안 고쳐도 전체가 개선된다. 안전망 성격.
- **이 프로젝트에는 아직 설정돼 있지 않다.** 넣을 만한 후보다.

### 고르는 기준

| 상황 | 선택 |
| :--- | :--- |
| 목록 조회, 응답 전용 | DTO 직접 조회 (3) |
| 엔티티가 필요하고 연관이 `@ManyToOne` | `JOIN FETCH` (1) 또는 `@EntityGraph` (2) |
| 페이징 + 컬렉션 | batch fetch size (4) |
| 전역 안전망 | (4) |

---

## 6. 어떻게 발견하는가

### 쿼리 수 세기 (이번에 쓴 방법)

`show_sql` 로그를 눈으로 세는 것보다 정확하다.

```java
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
```

```java
Statistics stats = entityManager.getEntityManagerFactory()
        .unwrap(SessionFactory.class).getStatistics();

stats.clear();
// ... 측정할 코드 ...
long queryCount = stats.getPrepareStatementCount();
```

측정 전에 반드시 `entityManager.clear()`를 부를 것. 1차 캐시에 이미 올라온 엔티티는 쿼리를 안 만들어서 **N+1이 없는 것처럼 보인다.**

### SQL 눈으로 보기

```yaml
spring:
  jpa:
    properties:
      hibernate:
        show_sql: true
        format_sql: true
```

`application-test.yml`에는 지금 `false`로 돼 있다. 공부할 때만 잠깐 켜면 된다.

### 회귀로 막기

숫자를 아는 순간 테스트로 고정할 수 있다.

```java
assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
```

건수를 늘려도 쿼리 수가 그대로인지 보는 편이 더 낫다 — 10건이든 100건이든 1회여야 한다.

---

## 7. 이 프로젝트에서 더 볼 만한 곳

- `BoardQueryDslRepository.findBoardList` — 목록에서 좋아요 수·댓글 수를 어떻게 가져오는지. 서브쿼리로 세는지, 조인해서 세는지에 따라 성능이 갈린다.
- 차단 필터를 붙이는 3~4번 단계 — `NOT IN` 서브쿼리가 목록 쿼리에 들어간다. 인덱스 유무에 따라 실행 계획이 달라진다.
- 이 프로젝트는 **게시판 계열에 인덱스를 의도적으로 두지 않았다.** N+1을 잡은 뒤 `EXPLAIN`으로 인덱스 전후를 비교하면 포트폴리오 소재가 된다 (`CLAUDE.md` 참고).

## 다음에 읽어볼 것

- `@BatchSize` — 엔티티/컬렉션 단위로 batch fetch 지정
- `MultipleBagFetchException`과 `Set` 사용
- OSIV(`spring.jpa.open-in-view`) — 켜져 있으면 컨트롤러·뷰에서 프록시가 깨어나 N+1이 더 늦게 드러난다
