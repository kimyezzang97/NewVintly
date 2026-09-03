package com.vintly.infra.vintagecomment;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VintageCommentRepositoryImpl implements VintageCommentRepository {

    private final VintageCommentJpaRepository jpaRepository;
    private final VintageCommentQueryDslRepository dslRepository;

    public VintageCommentRepositoryImpl(VintageCommentJpaRepository jpaRepository, VintageCommentQueryDslRepository dslRepository) {
        this.jpaRepository = jpaRepository;
        this.dslRepository = dslRepository;
    }

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    @Override
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId, List<Long> blockedIds) {
        return dslRepository.findCommentsByVintageId(vintageId, blockedIds);
    }

    @Override
    public void deleteAllCommentsByVintageId(Vintage vintage) {
        jpaRepository.deleteAllByVintage(vintage);
    }

    @Override
    public VintageComment save(VintageComment entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<VintageComment> findById(Long vintageCommentId) {
        return jpaRepository.findById(vintageCommentId);
    }

    @Override
    public void deleteById(Long vintageCommentId) {
        jpaRepository.deleteById(vintageCommentId);
    }

    @Override
    public void deleteAllByParentCommentId(Long parentCommentId) {
        jpaRepository.deleteAllByParentCommentId(parentCommentId);
    }

    @Override
    public void anonymizeMemberInComments(Member member, String deletedNickname) {
        jpaRepository.anonymizeMemberInComments(member, deletedNickname);
    }
}
