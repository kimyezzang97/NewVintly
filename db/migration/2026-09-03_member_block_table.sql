-- 2026-09-03 사용자 차단 기능 - member_block 테이블 생성
--
-- [배경]
-- 스토어 심사 대응. UGC 앱은 문제되는 사용자를 차단할 수단을 요구받는다.
-- 내가 차단한 회원의 글과 댓글이 내 화면에서만 사라지는 단방향 관계다.
-- 설계 배경은 docs/design/block.md 참고.
--
-- [적용 현황]
-- dev(NAS) : 미적용
-- prd      : 미적용
--
-- [설계 메모]
-- - 물리 FK 와 조회용 인덱스는 두지 않는다 (게시판 계열과 동일 방침).
--   UNIQUE 는 조회 성능이 아니라 중복 차단 차단이 목적인 정합성 장치라 예외다.
-- - updated_at 은 두지 않는다. 차단은 수정되지 않고 생기거나 사라진다.
-- - 탈퇴 시 이 회원이 걸린 행을 방향 상관없이 삭제한다. 차단은 감사 기록이 아니라
--   개인 설정이라 회원이 사라지면 남길 이유가 없다.
--   (같은 탈퇴 절차에서 report 는 반대로 그대로 남긴다 — 헷갈리지 말 것)
--
-- [참고] enum 컬럼이 없어 report 때와 달리 CHECK 제약 문제가 없다. ddl-auto=update 가
-- 만들어도 스키마가 같으므로 적용 순서 제약은 없다. 그래도 환경별 적용 여부를 남기기 위해
-- 이 파일을 정본으로 둔다.

CREATE TABLE IF NOT EXISTS member_block
(
    block_id   BIGINT      NOT NULL AUTO_INCREMENT,
    blocker_id BIGINT      NOT NULL COMMENT '차단한 회원 ID',
    blocked_id BIGINT      NOT NULL COMMENT '차단당한 회원 ID',
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (block_id),
    UNIQUE KEY uk_member_block_blocker_blocked (blocker_id, blocked_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 확인용
-- SELECT column_name, column_type, is_nullable, column_comment
--   FROM information_schema.columns
--  WHERE table_schema = DATABASE() AND table_name = 'member_block'
--  ORDER BY ordinal_position;
--
-- SHOW INDEX FROM member_block WHERE Key_name = 'uk_member_block_blocker_blocked';
-- 기대값: blocker_id, blocked_id 2개 컬럼, Non_unique = 0
--
-- 물리 FK 가 없는지 확인 (결과가 비어 있어야 한다)
-- SELECT constraint_name, table_name FROM information_schema.table_constraints
--  WHERE table_schema = DATABASE() AND constraint_type = 'FOREIGN KEY';
