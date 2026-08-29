# 유튜브 링크 API

빈티지 관련 유튜브 영상 링크 목록을 조회하는 API. 등록/수정/삭제 API는 없으며, 데이터는 서버에서 DB에 직접 등록한다.

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

## GET /api/v1/youtube-links

유튜브 링크 목록을 페이지네이션으로 조회한다. 최신 등록순(`createdAt desc`)으로 정렬되며, 정렬 기준은 변경할 수 없다.

### Query Parameters

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| page | int | N | 0 | 페이지 번호 (0부터 시작) |
| size | int | N | 10 | 페이지 크기 |

### Request 예시

```
GET /api/v1/youtube-links?page=0&size=10
access: {accessToken}
```

### Response

`200 OK`

```json
{
  "success": true,
  "code": 200,
  "msg": "",
  "data": {
    "content": [
      {
        "youtubeLinkId": 1,
        "url": "https://www.youtube.com/watch?v=xxxxxxxxxxx",
        "title": "90년대 빈티지 자켓 코디하는 법",
        "description": "빈티지 자켓 스타일링 팁 소개 영상",
        "createdAt": "2026-08-28T10:00:00",
        "updatedAt": "2026-08-28T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

#### data.content[] 필드

| 이름 | 타입 | 설명 |
|---|---|---|
| youtubeLinkId | Long | 유튜브 링크 ID |
| url | String | 유튜브 영상 URL |
| title | String | 제목 |
| description | String \| null | 설명 (없을 수 있음) |
| createdAt | String (ISO-8601 LocalDateTime) | 등록 시간 |
| updatedAt | String (ISO-8601 LocalDateTime) | 수정 시간 |

#### data 페이지 정보 필드

| 이름 | 타입 | 설명 |
|---|---|---|
| page | int | 현재 페이지 번호 (0부터 시작) |
| size | int | 요청한 페이지 크기 |
| totalElements | long | 전체 데이터 개수 |
| totalPages | int | 전체 페이지 수 |
| first | boolean | 첫 페이지 여부 |
| last | boolean | 마지막 페이지 여부 |

### 참고

- 서버 구현: `YoutubeLinkController` (`/api/v1/youtube-links`)
- Swagger UI에서도 동일 스펙 확인 가능 (`/swagger-ui/index.html`, Tag: `YoutubeLink`)
