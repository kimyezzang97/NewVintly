```mermaid
flowchart LR
    nickname확인 --> 이메일확인 --> 비밀번호조합--> 회원가입완료 --> 이메일인증
    이메일인증 --> 성공 --> 영구저장
    이메일인증 --> 실패 --> 1일후삭제
```
