package com.vintly.domain.vintagecomment.service;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.interfaces.vintagecomment.VintageCommentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VintageCommentServiceTest {

    @Mock
    private VintageCommentRepository vintageCommentRepository;

    @Mock
    private VintageRepository vintageRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private VintageCommentService vintageCommentService;

    private Member createMember(Long memberId) {
        return new Member(memberId, "test@example.com", "password", "nickname",
                "code", "ROLE_USER", Use.Y, null, null);
    }

    private Vintage createVintage(Long vintageId) {
        return mock(Vintage.class);
    }

    private VintageComment createRootComment(Long commentId, Member member, Vintage vintage) {
        return new VintageComment(commentId, vintage, member, 0L, "원본 댓글");
    }

    private VintageComment createReplyComment(Long commentId, Long parentId, Member member, Vintage vintage) {
        return new VintageComment(commentId, vintage, member, parentId, "대댓글");
    }

    @Nested
    @DisplayName("댓글 수정")
    class Update {

        @Test
        @DisplayName("본인의 댓글을 수정하면 내용이 변경된다")
        void shouldUpdateContentWhenOwner() {
            // Arrange
            Long commentId = 1L;
            Long memberId = 10L;
            Member member = createMember(memberId);
            Vintage vintage = createVintage(1L);
            VintageComment comment = createRootComment(commentId, member, vintage);

            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // Act
            vintageCommentService.update(commentId, memberId, "수정된 댓글");

            // Assert
            assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        }

        @Test
        @DisplayName("존재하지 않는 댓글을 수정하면 CommentNotFoundException이 발생한다")
        void shouldThrowWhenCommentNotFound() {
            // Arrange
            Long commentId = 999L;
            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> vintageCommentService.update(commentId, 10L, "수정"))
                    .isInstanceOf(VintageCommentException.CommentNotFoundException.class);
        }

        @Test
        @DisplayName("본인의 댓글이 아닌 댓글을 수정하면 CommentNotOwnedException이 발생한다")
        void shouldThrowWhenNotOwner() {
            // Arrange
            Long commentId = 1L;
            Long ownerId = 10L;
            Long otherMemberId = 20L;
            Member owner = createMember(ownerId);
            Vintage vintage = createVintage(1L);
            VintageComment comment = createRootComment(commentId, owner, vintage);

            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // Act & Assert
            assertThatThrownBy(() -> vintageCommentService.update(commentId, otherMemberId, "수정"))
                    .isInstanceOf(VintageCommentException.CommentNotOwnedException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class Delete {

        @Test
        @DisplayName("본인의 최상위 댓글을 삭제하면 대댓글도 함께 삭제된다")
        void shouldDeleteRootCommentWithReplies() {
            // Arrange
            Long commentId = 1L;
            Long memberId = 10L;
            Member member = createMember(memberId);
            Vintage vintage = createVintage(1L);
            VintageComment rootComment = createRootComment(commentId, member, vintage);

            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.of(rootComment));

            // Act
            vintageCommentService.delete(commentId, memberId);

            // Assert
            verify(vintageCommentRepository).deleteAllByParentCommentId(commentId);
            verify(vintageCommentRepository).deleteById(commentId);
        }

        @Test
        @DisplayName("본인의 대댓글을 삭제하면 해당 댓글만 삭제된다")
        void shouldDeleteOnlyReplyComment() {
            // Arrange
            Long replyId = 2L;
            Long parentId = 1L;
            Long memberId = 10L;
            Member member = createMember(memberId);
            Vintage vintage = createVintage(1L);
            VintageComment replyComment = createReplyComment(replyId, parentId, member, vintage);

            when(vintageCommentRepository.findById(replyId)).thenReturn(Optional.of(replyComment));

            // Act
            vintageCommentService.delete(replyId, memberId);

            // Assert
            verify(vintageCommentRepository, never()).deleteAllByParentCommentId(anyLong());
            verify(vintageCommentRepository).deleteById(replyId);
        }

        @Test
        @DisplayName("존재하지 않는 댓글을 삭제하면 CommentNotFoundException이 발생한다")
        void shouldThrowWhenCommentNotFound() {
            // Arrange
            Long commentId = 999L;
            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> vintageCommentService.delete(commentId, 10L))
                    .isInstanceOf(VintageCommentException.CommentNotFoundException.class);
        }

        @Test
        @DisplayName("본인의 댓글이 아닌 댓글을 삭제하면 CommentNotOwnedException이 발생한다")
        void shouldThrowWhenNotOwner() {
            // Arrange
            Long commentId = 1L;
            Long ownerId = 10L;
            Long otherMemberId = 20L;
            Member owner = createMember(ownerId);
            Vintage vintage = createVintage(1L);
            VintageComment comment = createRootComment(commentId, owner, vintage);

            when(vintageCommentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            // Act & Assert
            assertThatThrownBy(() -> vintageCommentService.delete(commentId, otherMemberId))
                    .isInstanceOf(VintageCommentException.CommentNotOwnedException.class);

            // 삭제 호출되지 않음
            verify(vintageCommentRepository, never()).deleteById(anyLong());
            verify(vintageCommentRepository, never()).deleteAllByParentCommentId(anyLong());
        }
    }
}
