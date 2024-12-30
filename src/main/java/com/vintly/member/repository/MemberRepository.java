package com.vintly.member.repository;

import com.vintly.entity.Member;
import com.vintly.member.model.constant.Use;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // email 중복 확인
    Integer countByEmail(String email);

    // 닉네임 중복 체크
    Integer countByNickname(String nickname);

    // 인증기간 지난 ID 삭제
    Integer deleteByCreateDateBeforeAndUseYn(LocalDateTime today, Use use);
}
