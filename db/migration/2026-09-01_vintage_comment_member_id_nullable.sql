-- 2026-09-01 회원탈퇴 하드 삭제 전환
--
-- [배경]
-- 탈퇴 시 vintage_comment의 작성자 링크를 끊기 위해 아래 쿼리가 실행된다.
--   UPDATE vintage_comment vc SET vc.member = null, vc.author_nickname = :del WHERE vc.member = :member
-- VintageComment 엔티티는 member_id를 @JoinColumn(nullable = true)로 선언하고 있으나,
-- ddl-auto=update 는 이미 존재하는 컬럼의 NULL 허용 여부를 변경하지 않으므로
-- 실제 DB 컬럼은 NOT NULL 로 남아 있었다.
-- 그 결과 매장 댓글을 작성한 적 있는 회원의 탈퇴가 항상 실패했다.
--   SQL Error 1048 (23000): Column 'member_id' cannot be null
--
-- [적용 현황]
-- dev(NAS, 222.117.117.110:3307/vintly) : 2026-09-01 적용 완료
-- prd                                   : 2026-09-01 적용 완료
--
-- [주의]
-- MODIFY COLUMN 은 컬럼 코멘트를 재지정하지 않으면 지워지므로 COMMENT 를 그대로 유지한다.
-- 물리 FK 는 원래 없으므로(@ForeignKey(ConstraintMode.NO_CONSTRAINT)) 제약 조건 처리는 불필요하다.

ALTER TABLE vintage_comment
    MODIFY COLUMN member_id BIGINT NULL COMMENT 'MEMBER 외래 키';

-- 확인용
-- SELECT column_name, column_type, is_nullable, column_comment
--   FROM information_schema.columns
--  WHERE table_schema = DATABASE()
--    AND table_name = 'vintage_comment'
--    AND column_name = 'member_id';
-- 기대값: is_nullable = YES
