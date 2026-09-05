-- 2026-09-03 신고 접수 기능 - report 테이블 생성
--
-- [배경]
-- 게시글/게시판 댓글/매장 댓글 신고를 대상별 테이블로 나누지 않고
-- target_type + target_id 조합으로 받는 단일 테이블이다.
-- 관리자 검토/제재는 후속 단계이며, 이번 범위는 접수까지다.
--
-- [적용 현황]
-- dev(NAS) : 미적용
-- prd      : 미적용
--
-- [중요] 배포 전에 반드시 이 스크립트를 먼저 실행할 것.
-- ddl-auto=update 는 테이블이 없으면 엔티티에서 만들어내는데, 그렇게 만들어진 테이블에는
-- Hibernate 가 enum 값 목록을 CHECK 제약으로 붙인다.
--   `reason` varchar(20) NOT NULL CHECK (`reason` in ('OBSCENE','ABUSE','SPAM','FLOOD','ETC'))
-- 그러면 신고 사유를 하나 추가할 때마다 환경별로 제약을 다시 손봐야 하고,
-- ddl-auto=update 는 기존 컬럼/제약을 바꾸지 않으므로 운영에서 조용히 저장이 실패한다.
-- 이 스크립트로 테이블을 먼저 만들어 두면 Hibernate 는 기존 테이블을 그대로 둔다.
--
-- [설계 메모]
-- - 물리 FK 와 조회용 인덱스는 두지 않는다 (게시판 계열과 동일 방침).
--   UNIQUE 는 조회 성능이 아니라 중복 신고 차단이 목적인 정합성 장치라 예외다.
-- - UNIQUE 에 target_type 이 반드시 포함돼야 한다. 빼면 board 100번을 신고한 사람이
--   board_comment 100번을 신고하지 못한다.
-- - reporter_id, target_id 는 orphan 을 허용한다. 신고자가 탈퇴하거나 대상이 삭제돼도
--   신고 이력은 남긴다 (감사 로그 성격).
-- - enum 은 varchar 로 저장한다. 네이티브 ENUM 이면 값 추가마다 ALTER 가 필요하다.

CREATE TABLE IF NOT EXISTS report
(
    report_id   BIGINT       NOT NULL AUTO_INCREMENT,
    reporter_id BIGINT       NOT NULL COMMENT '신고자 ID (탈퇴 시 orphaned, 신고 이력은 보존한다)',
    target_type VARCHAR(20)  NOT NULL COMMENT '신고 대상 종류',
    target_id   BIGINT       NOT NULL COMMENT '신고 대상 ID (대상 삭제 시 orphaned)',
    reason      VARCHAR(20)  NOT NULL COMMENT '신고 사유',
    detail      TEXT         NULL     COMMENT '상세 사유 (선택)',
    status      VARCHAR(20)  NOT NULL COMMENT '처리 상태',
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (report_id),
    UNIQUE KEY uk_report_reporter_target (reporter_id, target_type, target_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 이미 Hibernate 가 테이블을 만들어 버린 환경에서는 아래로 CHECK 제약을 걷어낸다.
-- 제약 이름은 자동 생성되므로 먼저 조회해 확인할 것.
--   SELECT constraint_name, check_clause
--     FROM information_schema.check_constraints
--    WHERE constraint_schema = DATABASE() AND table_name = 'report';
--   ALTER TABLE report DROP CONSTRAINT <constraint_name>;

-- 확인용
-- SELECT column_name, column_type, is_nullable, column_comment
--   FROM information_schema.columns
--  WHERE table_schema = DATABASE() AND table_name = 'report'
--  ORDER BY ordinal_position;
-- 기대값: target_type / reason / status 가 varchar(20)
--
-- SHOW INDEX FROM report WHERE Key_name = 'uk_report_reporter_target';
-- 기대값: reporter_id, target_type, target_id 3개 컬럼, Non_unique = 0
