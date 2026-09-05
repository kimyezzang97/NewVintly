# 신고 API

게시글·게시판 댓글·매장 댓글을 신고한다. 대상 종류에 상관없이 **엔드포인트 하나**로 접수하며, `targetType`으로 구분한다.

접수까지만 구현돼 있다. 관리자 검토·삭제·제재는 아직 없으므로, 신고 후 상태는 계속 `PENDING`이다. 앱에서 "처리 완료" 같은 상태 변화를 기대하지 말 것.

## 인증

- 로그인한 회원만 호출 가능
- 요청 헤더에 access 토큰을 담아 전달 (`Authorization: Bearer` 형식이 **아님**, 헤더 이름 자체가 `access`)

```
access: {accessToken}
```

- 미인증 시 응답: `401`
```json
{
  "success": false,
  "code": 401,
  "msg": "로그인이 필요합니다.",
  "data": null
}
```

- 토큰 만료: `401` / `msg: "access token expired"`
- 토큰 위변조·형식 오류: `401` / `msg: "invalid access token"`

## 오류 처리 방식 (중요)

**인증 오류(401)를 제외한 모든 오류는 HTTP 상태가 `200`으로 내려온다.** 실패 여부는 응답 본문의 `success` / `code`로 판단해야 한다. HTTP 상태 코드로 분기하면 중복 신고·권한 오류를 전부 성공으로 처리하게 된다.

```
if (httpStatus == 401)      → 재로그인 유도
else if (body.success)      → 성공
else                        → body.code 로 분기
```

이건 신고 API만의 규칙이 아니라 이 서버 전체의 응답 규약이다.

---

## POST /api/v1/reports

신고를 접수한다. 접수된 신고는 `PENDING` 상태로 저장된다.

### Request Body

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| targetType | String (enum) | Y | 신고 대상 종류. `BOARD` \| `BOARD_COMMENT` \| `VINTAGE_COMMENT` |
| targetId | Long | Y | 신고 대상 ID. 1 이상 |
| reason | String (enum) | Y | 신고 사유. `OBSCENE` \| `ABUSE` \| `SPAM` \| `FLOOD` \| `ETC` |
| detail | String | N | 상세 사유. 최대 500자 |

#### targetType — 어느 화면에서 무엇을 보내는가

| 화면 | targetType | targetId |
|---|---|---|
| 커뮤니티 게시글 | `BOARD` | `boardId` |
| 게시글의 댓글·대댓글 | `BOARD_COMMENT` | `boardCommentId` |
| 매장 상세의 댓글·대댓글 | `VINTAGE_COMMENT` | `vintageCommentId` |

`targetId`는 종류별로 별개의 ID 공간이다. 게시글 100번과 댓글 100번은 서로 다른 대상이며, 둘 다 신고할 수 있다.

#### reason — 표시 문구

| 값 | 문구 |
|---|---|
| OBSCENE | 음란물 |
| ABUSE | 욕설·비방 |
| SPAM | 광고·스팸 |
| FLOOD | 도배 |
| ETC | 기타 |

### Request 예시

```
POST /api/v1/reports
Content-Type: application/json
access: {accessToken}
```

```json
{
  "targetType": "BOARD",
  "targetId": 100,
  "reason": "ABUSE",
  "detail": "욕설이 포함되어 있습니다."
}
```

### Response

접수 성공 — HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "msg": "신고가 접수되었습니다.",
  "data": {
    "reportId": 10
  }
}
```

| 이름 | 타입 | 설명 |
|---|---|---|
| reportId | Long | 접수된 신고 ID |

### 실패 응답

전부 **HTTP 200**으로 내려온다. `data`는 항상 `null`.

| code | msg | 원인 | 앱 대응 |
|---|---|---|---|
| 409 | 이미 신고한 대상입니다. | 같은 대상을 이미 신고함 | "이미 신고한 콘텐츠입니다" 안내. 재시도 불필요 |
| 403 | 본인이 작성한 콘텐츠는 신고할 수 없습니다. | 자기 글·댓글을 신고 | 본인 콘텐츠에는 신고 버튼을 노출하지 않는 것이 바람직 |
| 404 | 신고 대상을 찾을 수 없습니다. | 대상이 이미 삭제됐거나 잘못된 ID | 목록 새로고침 유도 |
| 400 | 신고 대상 종류를 선택해주세요. | `targetType` 누락 | — |
| 400 | 신고 대상을 선택해주세요. | `targetId` 누락 | — |
| 400 | 신고 대상 ID가 올바르지 않습니다. | `targetId`가 0 이하 | — |
| 400 | 신고 사유를 선택해주세요. | `reason` 누락 | — |
| 400 | 상세 사유는 500자를 넘을 수 없습니다. | `detail` 500자 초과 | 입력창에서 미리 제한할 것 |

`msg`는 그대로 노출해도 되는 한국어 문구다.

### 동작 규칙

- **중복 신고 불가.** 같은 사람이 같은 대상을 두 번 신고할 수 없다. 사유를 바꿔도 마찬가지다.
- **본인 콘텐츠 신고 불가.**
- **탈퇴한 회원이 남긴 콘텐츠는 신고할 수 있다.** 글·댓글 자체는 남아 있으므로 삭제 대상이 될 수 있다. 작성자 표기는 `del_{memberId}`로 보인다.
- **신고해도 콘텐츠는 즉시 숨겨지지 않는다.** 블라인드 기능은 도입하지 않았다. 신고 후에도 해당 글·댓글은 그대로 노출된다.

---

## GET /api/v1/reports/me

로그인한 회원이 접수한 신고 목록을 최신순으로 조회한다. 페이지네이션 없이 전체를 반환한다.

### Request 예시

```
GET /api/v1/reports/me
access: {accessToken}
```

### Response

HTTP `200`

```json
{
  "success": true,
  "code": 200,
  "msg": "",
  "data": [
    {
      "reportId": 10,
      "targetType": "BOARD",
      "targetId": 100,
      "reason": "ABUSE",
      "detail": "욕설이 포함되어 있습니다.",
      "status": "PENDING",
      "createdAt": "2026-09-03T12:00:00"
    }
  ]
}
```

접수한 신고가 없으면 `data`는 빈 배열(`[]`)이다.

#### data[] 필드

| 이름 | 타입 | 설명 |
|---|---|---|
| reportId | Long | 신고 ID |
| targetType | String (enum) | `BOARD` \| `BOARD_COMMENT` \| `VINTAGE_COMMENT` |
| targetId | Long | 신고 대상 ID |
| reason | String (enum) | `OBSCENE` \| `ABUSE` \| `SPAM` \| `FLOOD` \| `ETC` |
| detail | String \| null | 상세 사유 (없을 수 있음) |
| status | String (enum) | 처리 상태. 현재는 항상 `PENDING` |
| createdAt | String (ISO-8601 LocalDateTime) | 접수 시간 |

#### status 값

| 값 | 문구 | 비고 |
|---|---|---|
| PENDING | 접수 | 현재 접수되는 모든 신고 |
| ACCEPTED | 처리 완료 | 관리자 검토 기능 구현 후 사용 |
| REJECTED | 기각 | 관리자 검토 기능 구현 후 사용 |

`ACCEPTED` / `REJECTED`는 아직 발생하지 않지만, 나중에 추가되면 기존 앱이 깨지지 않도록 **모르는 값이 와도 안전하게 처리**해 둘 것.

### 알려진 제약 — 대상 원문이 없다

응답에는 `targetType` / `targetId`만 있고 **신고한 콘텐츠의 제목이나 내용은 없다.** 내 신고 내역 화면에서 "무엇을 신고했는지" 보여주려면 앱이 별도로 대상을 조회해야 한다.

그런데 **대상이 이미 삭제됐을 수 있다.** 신고 이력은 감사 기록이라 대상이 사라져도 남기 때문이다. 조회가 404로 실패하는 경우를 정상 흐름으로 다뤄야 한다 ("삭제된 콘텐츠" 등으로 표시).

목록 화면에서 원문이 꼭 필요하다면 서버에 요청할 것 — 대상 요약(제목 일부)을 응답에 포함하는 방향으로 확장할 수 있다.

---

## 참고

- 서버 구현: `ReportController` (`/api/v1/reports`)
- Swagger UI에서도 동일 스펙 확인 가능 (`/swagger-ui/index.html`, Tag: `Report`)
- 호출 예시: `.http/report.http`
- 설계 배경과 후속 계획: `docs/design/report.md`
