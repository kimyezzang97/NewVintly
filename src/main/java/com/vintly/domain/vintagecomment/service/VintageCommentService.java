package com.vintly.domain.vintagecomment.service;

import com.vintly.interfaces.member.MemberException;
import com.vintly.domain.block.service.MemberBlockService;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.dto.VintageInfo;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.interfaces.block.MemberBlockException;
import com.vintly.interfaces.vintagecomment.VintageCommentException;
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
    private final MemberBlockService memberBlockService;

    // 빈티지 매장에 달린 댓글 리스트 조회 (최신순 정렬)
    public List<VintageInfo.Comment> findCommentsByVintageId(Long vintageId, List<Long> blockedIds) {
        return vintageCommentRepository.findCommentsByVintageId(vintageId, blockedIds);
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
        Member memberRef   = memberRepository.findById(memberId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        VintageComment newComment;

        // 최상위 댓글
        if (parentCommentId == null || parentCommentId == 0) {
            newComment = vintageCommentRepository.save(VintageComment.createRoot(vintageRef, memberRef, comment));
        }

        // 대댓글
        else {
            VintageComment parent = vintageCommentRepository.findById(parentCommentId)
                    .orElseThrow(VintageCommentException.ParentCommentNotFoundException::new);

            // 부모가 같은 매장인지 검증 (여기서 parent.getVintage() 접근하면서 쿼리 한 번 나감)
            if (!parent.getVintage().getVintageId().equals(vintageId)) {
                throw new VintageCommentException.ParentCommentMismatchException();
            }

            // 한 단계까지만 허용 (부모가 이미 대댓글이면 막기)
            if (parent.getParentCommentId() != 0L) {
                throw new VintageCommentException.ReplyDepthExceededException();
            }

            // 부모 댓글 작성자가 나를 차단했으면 답글을 달 수 없다 (설계 결정 6번).
            // 매장은 특정 회원의 소유가 아니라 최상위 댓글에는 검사할 대상이 없다.
            // 작성자가 이미 탈퇴했으면 member 가 null 이므로 통과시킨다.
            Member parentAuthor = parent.getMember();
            if (parentAuthor != null && parentAuthor.getMemberId() != null
                    && memberBlockService.isBlockedBy(memberId, parentAuthor.getMemberId())) {
                throw new MemberBlockException.BlockedByAuthorException();
            }

            newComment = vintageCommentRepository.save(VintageComment.createReply(vintageRef, memberRef, parentCommentId, comment));
        }

        VintageComment saved = vintageCommentRepository.save(newComment);
        return saved.getVintageCommentId();
    }


    // 댓글 수정 (본인 댓글만 수정 가능)
    @Transactional(rollbackFor = Exception.class)
    public void update(Long commentId, Long memberId, String content) {
        VintageComment comment = vintageCommentRepository.findById(commentId)
                .orElseThrow(VintageCommentException.CommentNotFoundException::new);

        if (comment.getMember() == null || !comment.getMember().getMemberId().equals(memberId)) {
            throw new VintageCommentException.CommentNotOwnedException();
        }

        comment.updateContent(content);
    }

    // 댓글 삭제 (본인 댓글만 삭제 가능, 부모 댓글 삭제 시 대댓글도 함께 삭제)
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId, Long memberId) {
        VintageComment comment = vintageCommentRepository.findById(commentId)
                .orElseThrow(VintageCommentException.CommentNotFoundException::new);

        if (comment.getMember() == null || !comment.getMember().getMemberId().equals(memberId)) {
            throw new VintageCommentException.CommentNotOwnedException();
        }

        // 최상위 댓글이면 대댓글도 함께 삭제
        if (comment.getParentCommentId() == 0L) {
            vintageCommentRepository.deleteAllByParentCommentId(commentId);
        }

        vintageCommentRepository.deleteById(commentId);
    }

}
