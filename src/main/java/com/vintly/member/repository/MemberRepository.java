package com.vintly.member.repository;

import com.vintly.entity.Member;
import com.vintly.member.model.constant.Use;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // email 중복 확인-
    Boolean existsByEmail(String email);

    // 닉네임 중복 확인
    Boolean existsByNickname(String nickname);

    // 인증기간 지난 ID 삭제
    Integer deleteByCreateDateBeforeAndUseYn(LocalDateTime today, Use use);

    // 이메일 코드, 이메일로 Member 엔티티 가져오기
    Optional<Member> findByEmailCodeAndEmail(String code, String email);

    // login 시 email(ID로 씀) 로 유저 정보 가져오기
    Optional<Member> findByEmail(String email);
}
