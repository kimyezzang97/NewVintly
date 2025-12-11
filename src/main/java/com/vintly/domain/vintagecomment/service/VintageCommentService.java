package com.vintly.domain.vintagecomment.service;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VintageCommentService {

    private final VintageCommentRepository vintageCommentRepository;
    private final VintageRepository vintageRepository;
    private final MemberRepository memberRepository;

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId) {
        return vintageCommentRepository.findCommentsByVintageId(vintageId);
    }

    public void deleteAllCommentsByVintage(Vintage vintage) {
        vintageCommentRepository.deleteAllCommentsByVintageId(vintage);
    }

    // 댓글 등록
    /** parentCommentId==0 이면 최상위 댓글, 아니면 대댓글 */
    @Transactional
    public Long create(Long vintageId, Long memberId, Long parentCommentId, String comment) {

        // 여기서는 진짜 조회할 필요 없이 프록시만 필요하니까 getReferenceById 사용
        Vintage vintageRef = vintageRepository.getReferenceById(vintageId);
        Member memberRef   = memberRepository.getReferenceById(memberId);

        VintageComment newComment;

        // 최상위 댓글
        if (parentCommentId == null || parentCommentId == 0) {
            newComment = vintageCommentRepository.save(VintageComment.createRoot(vintageRef, memberRef, comment));
        }

        // 대댓글
        else {
            VintageComment parent = vintageCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

            // 부모가 같은 매장인지 검증 (여기서 parent.getVintage() 접근하면서 쿼리 한 번 나감)
            if (!parent.getVintage().getVintageId().equals(vintageId)) {
                throw new IllegalStateException("부모 댓글이 다른 매장에 속해 있습니다.");
            }

            // 한 단계까지만 허용 (부모가 이미 대댓글이면 막기)
            if (parent.getParentCommentId() != 0L) {
                throw new IllegalStateException("대댓글에는 다시 대댓글을 달 수 없습니다.");
            }

            newComment = vintageCommentRepository.save(VintageComment.createReply(vintageRef, memberRef, parentCommentId, comment));
        }

        VintageComment saved = vintageCommentRepository.save(newComment);
        return saved.getVintageCommentId();
    }


    // 댓글 수정


    // 댓글 삭제

}
