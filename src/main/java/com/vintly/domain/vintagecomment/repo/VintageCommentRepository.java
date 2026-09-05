package com.vintly.domain.vintagecomment.repo;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.entity.VintageComment;

import java.util.List;
import java.util.Optional;

public interface VintageCommentRepository {

    // 해당 빈티지 매장의 댓글 리스트 조회
    List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId, List<Long> blockedIds);

    void deleteAllCommentsByVintageId(Vintage vintage);

    // 저장
    VintageComment save(VintageComment entity);

    Optional<VintageComment> findById(Long vintageCommentId);

    // 댓글 삭제
    void deleteById(Long vintageCommentId);

    // 부모 댓글 ID로 대댓글 삭제
    void deleteAllByParentCommentId(Long parentCommentId);

    // 회원 탈퇴 시 댓글 작성자 익명화 (member null, authorNickname = del_{memberId})
    void anonymizeMemberInComments(Member member, String deletedNickname);
}
