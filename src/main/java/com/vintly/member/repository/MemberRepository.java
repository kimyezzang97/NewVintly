package com.vintly.member.repository;

import com.vintly.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // email 중복 확인
    Integer countByEmail(String email);

}
