package com.vintly.infra.vintagelike;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagelike.entity.VintageLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VintageLikeJpaRepository extends JpaRepository<VintageLike, Long> {

    void deleteAllByVintage(Vintage vintage);

    long deleteByVintageAndMember(Vintage vintage, Member member);

    // 파생 쿼리(select 후 건별 delete) 대신 단일 delete 문으로 처리
    @Modifying
    @Query("DELETE FROM vintage_like vl WHERE vl.member = :member")
    void deleteAllByMember(@Param("member") Member member);
}
