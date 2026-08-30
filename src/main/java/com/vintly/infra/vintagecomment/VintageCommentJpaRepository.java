package com.vintly.infra.vintagecomment;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VintageCommentJpaRepository extends JpaRepository<VintageComment, Long> {

    void deleteAllByVintage(Vintage vintage);

    void deleteAllByParentCommentId(Long parentCommentId);

    @Modifying
    @Query("UPDATE vintage_comment vc SET vc.member = null, vc.authorNickname = :deletedNickname WHERE vc.member = :member")
    void anonymizeMemberInComments(@Param("member") Member member, @Param("deletedNickname") String deletedNickname);
}
