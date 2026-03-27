package com.vintly.domain.board.entity;

import com.vintly.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "board_comment")
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_comment_id")
    private Long boardCommentId;

    @Comment("게시글 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Comment("작성자 ID (탈퇴 시 orphaned, author_nickname은 del_{memberId}로 변경)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Comment("작성 당시 닉네임 (탈퇴 시 del_{memberId}로 변경)")
    @Column(name = "author_nickname", nullable = false, length = 30)
    private String authorNickname;

    @Comment("상위 댓글 ID (0이면 최상위)")
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Comment("댓글 내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static BoardComment createRoot(Board board, Member member, String content) {
        BoardComment c = new BoardComment();
        c.board = board;
        c.member = member;
        c.authorNickname = member.getNickname();
        c.parentId = 0L;
        c.content = content;
        return c;
    }

    public static BoardComment createReply(Board board, Member member, Long parentId, String content) {
        if (parentId == null || parentId <= 0)
            throw new IllegalArgumentException("parentId는 0보다 커야 합니다.");
        BoardComment c = new BoardComment();
        c.board = board;
        c.member = member;
        c.authorNickname = member.getNickname();
        c.parentId = parentId;
        c.content = content;
        return c;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
