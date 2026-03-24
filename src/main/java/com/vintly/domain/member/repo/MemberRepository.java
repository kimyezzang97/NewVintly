package com.vintly.domain.member.repo;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.Use;
import com.vintly.domain.vintage.entity.Vintage;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository {

    // email 중복 확인-
    Boolean existsByEmail(String email);

    // 닉네임 중복 확인
    Boolean existsByNickname(String nickname);

    // 인증기간 지난 ID 삭제
    Integer deleteByCreatedAtBeforeAndUseStatus(LocalDateTime today, Use use);

    // 이메일 코드, 이메일로 Member 엔티티 가져오기
    Optional<Member> findByEmailCodeAndEmail(String code, String email);

    // login 시 email(ID로 씀) 로 유저 정보 가져오기
    Optional<Member> findByEmail(String email);

    Member save(Member member);

    void delete(Member member);

    void deleteAll();

    // 조회 쿼리 없이 프록시로 참조만 걸기
    Member getReferenceById(Long memberId);
}
